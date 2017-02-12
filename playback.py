import numpy as np
import cv2

cap = cv2.VideoCapture('output.avi')

while(cap.isOpened()):
    ret, frame = cap.read()

    cv2.imshow('frame',frame)
    if cv2.waitKey(30) & 0xFF == ord(' '):
        break

cap.release()
cv2.destroyAllWindows()
