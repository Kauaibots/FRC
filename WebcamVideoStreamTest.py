import cv2
import time
from WebcamVideoStream import WebcamVideoStream
from FPS import FPS

vs = WebcamVideoStream(src=0).start()
fps = FPS().start()

while (vs.isOpened()):
	
	ret, resized_frame = vs.read_resized(320, 240)
	if ret != True:
		# If frame not available (or a Camera IO error occurs) try again
		time.sleep(0.05)
		continue
		
	cv2.imshow("Resized Frame", resized_frame)
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
