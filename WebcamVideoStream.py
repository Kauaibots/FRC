# import the necessary packages
from threading import Thread, Lock
import time
import cv2
 
class WebcamVideoStream:
	def __init__(self, src=0):
		# initialize the video camera stream and read the first frame
		# from the stream
		self.stream = cv2.VideoCapture(src)
		(self.grabbed, self.frame) = self.stream.read()
 
		# initialize the variable used to indicate if the thread should
		# be stopped
		self.stopped = False
		
		# initialize synchronization Lock
		self.lock = Lock()
		self.frame_copy = None
		
	def start(self):
		# start the thread to read frames from the video stream
		self.thread = Thread(target=self.update, args=())
		self.thread.start()
		return self	
 
	def isOpened(self):
		return self.stream.isOpened()
 
	def update(self):
		# keep looping infinitely until the thread is stopped
		while True:
			# if the thread indicator variable is set, stop the thread
			if self.stopped:
				self.stream.release() # Release opencv resources
				return
 
			(self.grabbed, self.frame) = self.stream.read()
			# otherwise, read the next frame from the stream
			self.lock.acquire()
			try:
				if(self.grabbed == True):
					self.frame_copy = self.frame
				self.frame = None
			finally:
				self.lock.release() # release lock, no matter what

	def read_resized(self, width, height):	
		# return a resized copy of the frame most recently read
		# returned frame is only valid if the first return result (grabbed) is True
		temp_grabbed = False
		resized_frame = None
		self.lock.acquire()
		try:
			if self.grabbed:
				if self.frame_copy != None:			
					temp_grabbed = True
					resized_frame = cv2.resize(self.frame_copy, (width, height))
					self.frame_copy = None # Encourage python to reclaim frame memory
					self.grabbed = False
		finally:
			self.lock.release() # release lock, no matter what
		return temp_grabbed, resized_frame
 
	def stop_and_wait(self):
		# indicate that the thread should be stopped
		self.stopped = True
		self.thread.join() # wait for thread to stop
		