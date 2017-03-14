import sys
import numpy as np
import math
import cv2
import time
import networktables
from time import sleep
from networktables import NetworkTables

#Video input. '0' for default camera
video = cv2.VideoCapture(0)

#Mat for cv2.open() and cv2.close
element = cv2.getStructuringElement(cv2.MORPH_RECT, (5, 5), (2,2))

#World coordinates of outer corners of reflective tape in inches
model_points_outer = np.array([
                            (-5.125, -2.5, 0.0),    # bottom left
                            (-5.125, 2.5, 0.0),     # top left
                            (5.125, 2.5, 0.0),      # top right
                            (5.125, -2.5, 0.0),     # bottom right
                        ])

#NetworkTables initialization
ip = sys.argv[1]
NetworkTables.initialize(server=ip)
sd = NetworkTables.getTable("SmartDashboard")

#Video writing initialization
fourcc = cv2.VideoWriter_fourcc(*'XVID')
outputFile = cv2.VideoWriter(time.strftime("%d_%H_%M_%S",time.gmtime()) + '.avi',fourcc,10.0,(640,480))
sd.putBoolean('Timer', True)
fileOpen = False

#Main loop. Breaks if camera is unplugged
while(video.isOpened()):
    
    #Get frame
    ret, frame = video.read()

    #Video writing.
    #If game running and file is open then write
    gameRunning = sd.getBoolean('Timer')==True
    if ret==True and gameRunning==True and fileOpen==True:
        outputFile.write(frame)
    #If game running and file is not open then open file
    elif ret==True and gameRunning == True and fileOpen == False:
        outputFile = cv2.VideoWriter(time.strftime("%d_%H_%M_%S",time.gmtime()) + '.avi',fourcc,10.0,(640,480))
        fileOpen = True
    #Else close file
    else:
        outputFile.release()
        fileOpen = False

    #Split colors into red, green, and blue
    b,g,r = cv2.split(frame)

    #Threshold green color
    ret,threshold = cv2.threshold(g,240,255,cv2.THRESH_BINARY)

    #Lower extra noise
    opening = cv2.morphologyEx(threshold, cv2.MORPH_OPEN,element)
    closing = cv2.morphologyEx(opening, cv2.MORPH_CLOSE,element)

    #Find contours
    im2, input_contours, hierarchy = cv2.findContours(closing, mode=cv2.RETR_LIST, method=cv2.CHAIN_APPROX_SIMPLE)

    #Loop through and filter contours
    filtercontour = []
    allPoints = []
    loopCount = 0

    for contour in input_contours:
        
        #Filter contours by bounding box w/h ratio
        x,y,w,h = cv2.boundingRect(contour)
        ratio = (float)(w) / h
        if (ratio < 0.1 or ratio > 0.7):
            continue

        #Add to list of filtered contours
        filtercontour.append(contour)

        #Approximate contour to polygon
        epsilon = 0.05*cv2.arcLength(contour,True)
        approx = cv2.approxPolyDP(contour,epsilon,True)

        #Add contour corners to list AllPoints
        for i in range(0,len(approx)):
            center = (int(approx[i][0][0]),int(approx[i][0][1]))
            allPoints.append([int(approx[i][0][0])-(0),int(approx[i][0][1])-(0)])

        #Camera calibration coeficients
        size = frame.shape
        focal_length = size[1]
        center = (size[1]/2, size[0]/2)
        camera_matrix = np.array(
                         [[1288.4307, 0, 320],
                         [0.0, 1288.4307, 240.0],
                         [0.0, 0.0, 1.0]],dtype = "double"
                         )
        dist_coeffs = np.array([
                            0.9104,
                            -23.9855,
                            -0.0459,
                            0.02901,
                            150.6836
                        ], dtype="double")
                   
    #Set 'Sees?' NetworkTable key
    if len(allPoints) >= 4:
            sd.putBoolean ("Sees?", True)
            print ("Sees?", 1)
    else:
            sd.putBoolean ("Sees?", False)
            print ("Sees?", 0)

    #Run if 'successfully' found target
    if (len(filtercontour) > 1):

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

        #Use SolvePNP and Rodrigues to get pose in world coordinates
        (success, rvec, tvec) = cv2.solvePnP(model_points_outer, image_points, camera_matrix, dist_coeffs)
        dst, jacobian = cv2.Rodrigues(rvec)
        x = tvec[0][0]
        y = tvec[2][0]/2
        t = math.asin(-dst[0][2])

        Rx = y * (math.cos((math.pi/2) - t))
        Ry = y * (math.sin((math.pi/2) - t))
        
        #Write mjpeg to Desktop
        #cv2.imwrite('/home/pi/Desktop/mjpeg.jpg', frame)

        #Write pose to NetworkTables
        sd.putNumber('x', Rx)
        sd.putNumber('y', Ry)
        sd.putNumber('z', np.rad2deg (t))

#Close video
video.release()
outputFile.release()
