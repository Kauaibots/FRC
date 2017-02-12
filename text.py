import numpy as np
import cv2

vc = cv2.VideoCapture(0)

while(vc.isOpened()):
    ret, frame = vc.read()

    resize = cv2.resize(frame, (400,225), 0, 0, cv2.INTER_LINEAR)
    out = cv2.cvtColor(resize, cv2.COLOR_BGR2HSV)
    threshold = cv2.inRange(out, (0.0,0.0,217.0),(170.0,76.0,255.0))
    erode = cv2.erode(threshold, None, anchor = (-1,-1), iterations = 2, borderType = cv2.BORDER_CONSTANT, borderValue = (-1))
    dilate = cv2.dilate(erode, None, anchor = (-1,-1), iterations = 4, borderType = cv2.BORDER_CONSTANT, borderValue = (-1))
    erode2 = cv2.erode(dilate, None, anchor = (-1,-1), iterations = 2, borderType = cv2.BORDER_CONSTANT, borderValue = (-1))
    im2, input_contours, hierarchy =cv2.findContours(erode2, mode=cv2.RETR_LIST, method=cv2.CHAIN_APPROX_SIMPLE)
    filtercontour = []
    #bbox = 0
    loopCount = 0
    for contour in input_contours:
        loopCount = loopCount + 1
        x,y,w,h = cv2.boundingRect(contour)
        if (w < 0.0 or w > 1000.0): # w < minWidth or w > maxWidth
            continue
        if (h < 0.0 or h > 1000.0): # h < minHeight or h > maxHeight
            continue
        area = cv2.contourArea(contour)
        if (area < 200.0): # area < min_area
            continue
        if (cv2.arcLength(contour, True) < 0): # cv2.arcLength(contour, True) < min_perimeter
            continue
        hull = cv2.convexHull(contour)
        solid = 100 * area / cv2.contourArea(hull)
        if (solid < 0.0 or solid > 100.0): # solid < solidity[0] or solid > solidity[1]
            continue
        if (len(contour) < 0.0 or len(contour) > 1000000.0): # len(contour) < min_vertex_count or len(contour) > max_vertex_count
            continue
        ratio = (float)(w) / h
        if (ratio < 0.0 or ratio > 1000.0): # ratio < min_ratio or ratio > max_ratio
            continue
        filtercontour.append(contour)
        rect = cv2.minAreaRect(contour)
        box = cv2.boxPoints(rect)
        box = np.int0(box)
        bbox = cv2.drawContours(resize,[box],-1,(0,0,255),2)
        cv2.putText(bbox, 'Width = ' + str(w),(10,0+(loopCount*15)), cv2.FONT_HERSHEY_SIMPLEX, 0.45,(0,0,255),1,cv2.LINE_AA)

    if (len(input_contours) > 0):
    

        #drawcontour =  cv2.drawContours(resize, filtercontour, -1, (0,0,255), 2)
   
        cv2.imshow('frame',bbox)
    if cv2.waitKey(30) & 0xFF == ord(' '):
        break

vc.release()
cv2.destroyAllWindows()
