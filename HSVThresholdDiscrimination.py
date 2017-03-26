import sys
import numpy as np
import math
import cv2
import time
import networktables
from time import sleep
from networktables import NetworkTables
from WebcamVideoStream import WebcamVideoStream
from FPS import FPS

vs = WebcamVideoStream(src='Positions26_01_57_30.avi').start()
fps = FPS().start()

debug = True
write = False
frame_width = 320
frame_height = 240

hMin = 0
hMax = 180
sMin = 0
sMax = 22
vMin = 248
vMax = 255

#Mat for cv2.open() and cv2.close
element = cv2.getStructuringElement(cv2.MORPH_RECT, (5, 5), (2,2))

#World coordinates of outer corners of reflective tape in inches
model_points_outer = np.array([
                            (-5.125, -2.5, 0.0),    # bottom left
                            (-5.125, 2.5, 0.0),     # top left
                            (5.125, 2.5, 0.0),      # top right
                            (5.125, -2.5, 0.0),     # bottom right
                        ])

#World coordinates of outer corners of left reflective tape in inches
left_model_points_outer = np.array([
                            (-5.125, -2.5, 0.0),    # bottom left
                            (-5.125, 2.5, 0.0),     # top left
                            (-3.125, 2.5, 0.0),      # top right
                            (-3.125, -2.5, 0.0),     # bottom right
                        ])

#Camera calibration coeficients
camera_matrix = np.array(
                     [[970.006, 0, 320],
                     [0.0, 970.006, 240.0],
                     [0.0, 0.0, 1.0]],dtype = "double"
                     )
dist_coeffs = np.array([
                        0.5474,
                        -6.9695,
                        0.0768,
                        0.02104,
                        23.6695
                    ], dtype="double")


#NetworkTables initialization
ip = sys.argv[1]
NetworkTables.initialize(server=ip)
sd = NetworkTables.getTable("SmartDashboard")

#Video writing initialization
fourcc = cv2.VideoWriter_fourcc(*'XVID')
outputFile = cv2.VideoWriter(time.strftime("%d_%H_%M_%S",time.gmtime()) + '.avi',fourcc,10.0,(640,480))
sd.putBoolean('Timer', True)
fileOpen = False

while (vs.isOpened()):
	
	ret, frame = vs.read_resized(frame_width, frame_height)
	if ret != True:
		# If frame not available (or a Camera IO error occurs) try again
		time.sleep(0.05)
		continue

        size = frame.shape
        focal_length = size[1]
        center = (size[1]/2, size[0]/2)

	#Video writing.
        #If game running and file is open then write
        gameRunning = sd.getBoolean('Timer')==True
        if ret==True and gameRunning==True and fileOpen==True:
                if write:
                        outputFile.write(frame)

                #TODO: Fix video writing
                
        #If game running and file is not open then open file
        elif ret==True and gameRunning == True and fileOpen == False:
                outputFile = cv2.VideoWriter(time.strftime("%d_%H_%M_%S",time.gmtime()) + '.avi',fourcc,10.0,(640,480))
                fileOpen = True
        #Else close file
        else:
                outputFile.release()
                fileOpen = False

        #resize = cv2.resize(frame, (400,225), 0, 0, cv2.INTER_LINEAR)
        out = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
        threshold = cv2.inRange(out, (hMin,sMin,vMin),(hMax,sMax,vMax))
        erode = cv2.erode(threshold, None, anchor = (-1,-1), iterations = 1, borderType = cv2.BORDER_CONSTANT, borderValue = (-1))
        dilate = cv2.dilate(erode, None, anchor = (-1,-1), iterations = 1, borderType = cv2.BORDER_CONSTANT, borderValue = (-1))

        #For threshold overlay during debugging
        if debug:
                threshold = cv2.merge((dilate, dilate, dilate));
                approxIm = cv2.addWeighted(frame,0.5,threshold,0.5,0)
        else:
                approxIm = frame
                
        #Find contours
        im2, input_contours, hierarchy = cv2.findContours(dilate, mode=cv2.RETR_LIST, method=cv2.CHAIN_APPROX_SIMPLE)

        #Loop through and filter contours
        filtercontour = []
        allPoints = []
        loopCount = 0

        for contour in input_contours:

                #Filter contours by bounding box w/h ratio
                x,y,w,h = cv2.boundingRect(contour)
                ratio = (float)(w) / h
                if (ratio < 0.3 or ratio > 0.6):
                        #pass
                        continue

                #Filter contours by area.
                area = cv2.contourArea(contour)
                if (area < 250.0):
                        #pass
                        continue

                minAreaRect = cv2.minAreaRect(contour)
                minAreaRect = cv2.boxPoints(minAreaRect)
                minAreaRect = np.int0(minAreaRect)
                rectangularity = area/cv2.contourArea(minAreaRect)
                if (rectangularity < 0.6):
                        pass
                        #continue

                #Approximate contour to polygon
                epsilon = 0.075*cv2.arcLength(contour,True)
                approx = cv2.approxPolyDP(contour,epsilon,True)

                if (len(approx) <= 3):
                        pass
                        #continue

                if debug:
                        print("Ratio:",ratio)
                        print("Area:",area)
                        print("Rectangularity:",rectangularity)
                        print("Points:",len(approx))


                #Add contour corners to list AllPoints
                for i in range(0,len(approx)):
                    center = (int(approx[i][0][0]),int(approx[i][0][1]))
                    allPoints.append([int(approx[i][0][0])-(0),int(approx[i][0][1])-(0)])

                if (len(approx)>3):
                    filtercontour.append(approx)

        approxIm = cv2.drawContours(approxIm, filtercontour, -1, (0,0,255), 1)
                       
        #Set 'Sees?' NetworkTable key
        if len(allPoints) >= 4:
                sd.putBoolean ("Sees?", True)
        else:
                sd.putBoolean ("Sees?", False)

        #Run if 'successfully' found target
        if (len(filtercontour) > 0):

                #Find outer corners of tape
                maxVal = -99999
                minVal =  99999

                for i in range(0,len(allPoints)):
                    val = allPoints[i][0] + allPoints[i][1]
                    if val > maxVal:
                        maxVal = val
                        maxPoint = i
                    if val < minVal:
                        minVal = val
                        minPoint = i
                bottomRight =  allPoints[maxPoint]
                topLeft = allPoints[minPoint]

                maxVal = -99999
                minVal =  99999

                for i in range(0,len(allPoints)):
                    val = allPoints[i][0] - allPoints[i][1]
                    if val > maxVal:
                        maxVal = val
                        maxPoint = i
                    if val < minVal:
                        minVal = val
                        minPoint = i
                bottomLeft =  allPoints[minPoint]
                topRight = allPoints[maxPoint]

                #Create list of outer corner image coordinates
                image_points = np.array([
                                    bottomLeft,
                                    topLeft, 
                                    topRight,
                                    bottomRight,
                                ], dtype="double")

                for i in range(0,4):
                    center = (int(image_points[i][0]),int(image_points[i][1]))
                    approxIm = cv2.circle(approxIm,center,4,(0,255,0),1)

                if (len(filtercontour) > 1):
                    print("Multiple targets")
                    (success, rvec, tvec) = cv2.solvePnP(model_points_outer, image_points, camera_matrix, dist_coeffs)

                else:
                    print("One target")
                    (success, rvec, tvec) = cv2.solvePnP(left_model_points_outer, image_points, camera_matrix, dist_coeffs)


                #Use SolvePNP and Rodrigues to get pose in world coordinates
                #(success, rvec, tvec) = cv2.solvePnP(model_points_outer, image_points, camera_matrix, dist_coeffs)
                dst, jacobian = cv2.Rodrigues(rvec)
                x = tvec[0][0]
                y = tvec[2][0]/2
                t = math.asin(-dst[0][2])

                Rx = y * (math.cos((math.pi/2) - t))
                Ry = y * (math.sin((math.pi/2) - t))

                #Write pose to NetworkTables
                sd.putNumber('x', Rx)
                sd.putNumber('y', Ry)
                sd.putNumber('z', np.rad2deg (t))
                if debug:
                        print("X", Rx)
                        print("Y", Ry)
                        print("Z", np.rad2deg (t))
                        print("")

        if debug:
                cv2.imshow("Resized Frame", approxIm)

        #Write mjpeg to Desktop
        cv2.imwrite('/home/pi/Desktop/mjpeg.jpg', frame)
        
        if cv2.waitKey(30) & 0xFF == ord(' '):
                break
                
        # update the FPS counter
        fps.update()

		
# stop the timer and display FPS information
fps.stop()
print("[INFO] elapsed time: {:.2f}".format(fps.elapsed()))
print("[INFO] approx. FPS: {:.2f}".format(fps.fps()))
 
# do a bit of cleanup
cv2.destroyAllWindows()	# Cleanup Video Display Window
vs.stop_and_wait() # Stop the WebcamVideoStream thread, wait for the thread to quit
