#include "opencv2/opencv.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include <stdio.h>
#include <math.h>
#include <iostream>
#include <string>
#include <climits>
#include <errno.h>

using namespace cv;

void process_frame_shield_divider( Mat& frame, Mat& grayscale, Mat& grayscale_blur, Mat& cdst, Mat& edges );
void process_retroreflective_tape( Mat& frame, Mat& grayscale, Mat& grayscale_blur, Mat& cdst, Mat& edges );
void process_tower_openings( Mat& frame, Mat& grayscale, Mat& grayscale_blur, Mat& cdst, Mat& edges );

int process_circles(Mat& src, Mat& grayscale, Mat& cdst);

#define ALGORITHM_SHIELD_DIVIDER               1
#define ALGORITHM_TOWER_RETROREFLECTIVE_TAPE   2
#define ALGORITHM_TOWER_OPENING                3
#define ALGORITHM_FIRST			       1
#define ALGORITHM_LAST                         ALGORITHM_TOWER_OPENING			      

int camera_main(char *video_file, int algorithm);
int image_main(char *img, int algorithm);

//#define printf(...)

int main(int argc, char** argv)
{
	if ( argc < 2 ) {
		printf("Parameters:  <1-3> <filename>\n");
		return -1;
	}
	int algorithm = atoi(argv[1]);
	if ( ( algorithm < ALGORITHM_FIRST ) ||
	     ( algorithm > ALGORITHM_LAST ) ) {
		printf("Invalid algorithm number.\n");
		return -2;
	}
	if ( argc > 2 ) {
		std::string arg(argv[2]);
		if( (arg.substr(arg.find_last_of(".") + 1) == "mp4") ||
		    (arg.substr(arg.find_last_of(".") + 1) == "avi") ||
                    (arg == "0") ||
                    (arg == "1")) {
			camera_main(argv[2], algorithm);
		} else {
			image_main(argv[2], algorithm);
		}
	}
}

int image_main(char *img, int algorithm) 
{
	Mat edges;
	Mat grayscale;
	Mat grayscale_blur;
	Mat cdst;
        Mat cdst2;
	namedWindow("edges",1);
	namedWindow("lines",1);

	Mat inframe = imread(img, CV_LOAD_IMAGE_COLOR);
	Mat frame;

	Size size(640,480);//the dst image size
	resize(inframe,frame,size);//resize image
	if ( algorithm == ALGORITHM_SHIELD_DIVIDER ) {
		process_frame_shield_divider(frame, grayscale, grayscale_blur, cdst, edges);
	} else if ( algorithm == ALGORITHM_TOWER_RETROREFLECTIVE_TAPE ) {
		process_retroreflective_tape(frame, grayscale, grayscale_blur, cdst, edges);
	} else if ( algorithm == ALGORITHM_TOWER_OPENING ) {
		process_tower_openings(frame, grayscale, grayscale_blur, cdst, edges);
	}
	//process_circles(frame, grayscale, cdst);	
	waitKey(0);

	return -1;
}

int camera_main(char *video_file, int algorithm)
{
    printf("camera_main()\n");
    VideoCapture *cap;
    if ( video_file == NULL ) {
        cap = new VideoCapture(0); // open the default camera
    } else {
        char *p;
        long vid_device_number = strtol(video_file,&p,10);
        if ((p != video_file && *p != '\0') && (errno == ERANGE)) {
            cap = new VideoCapture(video_file);
        } else {
            cap = new VideoCapture((int)vid_device_number);
        }
    }
    printf("VideoCapture instantiated.\n");
    if(!cap->isOpened())  // check if we succeeded
        return -1;

    printf("Opened camera.\n");
    cap->set(CV_CAP_PROP_FRAME_WIDTH,1280);
    cap->set(CV_CAP_PROP_FRAME_HEIGHT,720);

    printf("Frame Width:  %i\n", (int)cap->get(CV_CAP_PROP_FRAME_WIDTH));
    printf("Frame Height:  %i\n", (int)cap->get(CV_CAP_PROP_FRAME_HEIGHT));
    printf("FPS:  %i\n", (int)cap->get(CV_CAP_PROP_FPS));

    Mat edges;
    Mat grayscale;
    Mat grayscale_blur;
    Mat cdst;
    namedWindow("edges",1);
    namedWindow("lines",1);
    for(;;)
    {
        Mat inframe;
	Mat frame;

        *cap >> inframe; // get a new frame from camera
	Size size(640,480);//the dst image size
	resize(inframe,frame,size);//resize image

	if ( algorithm == ALGORITHM_SHIELD_DIVIDER ) {
		process_frame_shield_divider(frame, grayscale, grayscale_blur, cdst, edges);
	} else if ( algorithm == ALGORITHM_TOWER_RETROREFLECTIVE_TAPE ) {
		process_retroreflective_tape(frame, grayscale, grayscale_blur, cdst, edges);
	} else if ( algorithm == ALGORITHM_TOWER_OPENING ) {
		process_tower_openings(frame, grayscale, grayscale_blur, cdst, edges);
	}
	if (video_file == NULL) {
	        if(waitKey(30) >= 0) break;
	} else {
		if(waitKey(2) >= 0) break;
	}
    }
    // the camera will be deinitialized automatically in VideoCapture destructor
    delete cap;
}

#define BORDER_WIDTH_PIXELS 0
#define FRAME_HEIGHT_PIXELS 480
#define FRAME_WIDTH_PIXELS  640

float angle_between(const Point &v1, const Point &v2)
{
	double Angle = atan2(v2.y - v1.y,v2.x - v1.x) * 180.0 / CV_PI;
    	if(Angle<0) Angle=Angle+360;

	/* Rotate by 90 degrees so that the top of the image is 0 degrees */
	Angle += 90;
	if (Angle>360) Angle=Angle-360;

	return (float)Angle;
}

void project_line(const Point &v1, const Point &v2, Point &out_begin, Point& out_end)
{
	float slope =
		((float)(v2.y-v1.y)) /
			(v2.x-v1.x);
	printf("slope:  %f\n",slope);
	printf("p1:  %i,%i, p2:  %i,%i\n",
		v1.x, v1.y,
		v2.x, v2.y);

	if (isinf(slope)) {
		if ( v1.x == v2.x ) {
			out_begin.x = 0;
			out_end.x = FRAME_HEIGHT_PIXELS - 1;
		} else if ( v1.y == v2.y ) {
			out_begin.y = 0;
			out_end.y = FRAME_WIDTH_PIXELS - 1;
		}
		return;
	}

	out_begin.x = 0;
	out_begin.y = v1.y - (v1.x * slope);
	if ( out_begin.y < 0 ) {
		// Y coordinate negative (out of image frame to left of image)
		out_begin.x = out_begin.y / -slope; /* Negative slope */
		out_begin.y = 0;
	} else if ( out_begin.y > (FRAME_HEIGHT_PIXELS-1) ) {
		out_begin.x = v1.x + (((FRAME_HEIGHT_PIXELS-1) - v1.y) / slope); /* Positive slope */
		out_begin.y = FRAME_HEIGHT_PIXELS - 1;
	}
	if ( out_begin.x < 0 ) out_begin.x = 0;
	out_end.x = FRAME_WIDTH_PIXELS-1;
	out_end.y = v2.y + (((FRAME_WIDTH_PIXELS-1) - v2.x) * slope);
	if ( out_end.y < 0 ) {
		// Y coordinate negative (out of image frame to right of image)
		out_end.x = FRAME_WIDTH_PIXELS - (out_end.y / slope); /* Negative slope */
		out_end.y = 0;
	} else if ( out_end.y > (FRAME_HEIGHT_PIXELS-1) ) {
		out_end.x = v2.x + (((FRAME_HEIGHT_PIXELS-1) - v2.y) / slope); /* Positive slope */
		out_end.y = FRAME_HEIGHT_PIXELS - 1;
	}
}

// Finds the intersection of two lines, or returns false.
// The lines are defined by (o1, p1) and (o2, p2).
bool intersection(Point& o1, Point& p1, Point& o2, Point& p2,
                      Point& r)
{
    Point x = o2 - o1;
    Point d1 = p1 - o1;
    Point d2 = p2 - o2;

    float cross = d1.x*d2.y - d1.y*d2.x;
    printf("Cross:  %f\n", cross);
    if (abs(cross) < /*EPS*/1e-10)
        return false;

    double t1 = (x.x * d2.y - x.y * d2.x)/cross;
    r = o1 + d1 * t1;
    return true;
}

double cross(Point v1,Point v2){
    return v1.x*v2.y - v1.y*v2.x;
}

bool getIntersectionPoint(Point a1, Point a2, Point b1, Point b2, Point & intPnt){
    Point p = a1;
    Point q = b1;
    Point r(a2-a1);
    Point s(b2-b1);

    if(cross(r,s) == 0) {return false;}

    double t = cross(q-p,s)/cross(r,s);

    intPnt = p + t*r;
    return true;
}

bool point_on_line(Point p1, Point p2, Point p3, int tolerance) // returns true if p3 is on line p1, p2
{
	Point va = p1 - p2;
	Point vb = p3 - p2;
	int area = va.x * vb.y - va.y * vb.x;
	printf("Area:  %i\n",abs(area));
	if (abs (area) < tolerance)
		return true;
	return false;
}

int rectangle_area(Point p1, Point p2, Point p3, Point p4) 
{
	/* Area of first triangle */
	Point va = p1 - p2;
	Point vb = p3 - p2;
	int area_tri1 = abs(va.x * vb.y - va.y * vb.x);
	/* Area of second triangle */
	Point vc = p2 - p4;
	Point vd = p4 - p3;
	int area_tri2 = abs(vc.x * vd.y - vc.y * vd.x);
	
	return area_tri1 + area_tri2;
}

void process_tower_openings( Mat& frame, Mat& grayscale, Mat& grayscale_blur, Mat& cdst, Mat& edges )
{
	cvtColor(frame, grayscale, COLOR_BGR2GRAY);

	std::vector<cv::Vec3f> circles;
	Mat thresholded_image;

	GaussianBlur(grayscale, grayscale_blur, Size(15,15), 1.5, 1.5);
	Canny(grayscale_blur, edges, 20, 80, 3);
	cvtColor(edges, cdst, CV_GRAY2BGR);
	imshow("edges", cdst);

	threshold(edges, thresholded_image, 254, 255, THRESH_BINARY);

	vector<vector<Point> > contours;
	vector<Vec4i> hierarchy;

	/// Find contours
	findContours( thresholded_image, contours, hierarchy, /*CV_RETR_EXTERNAL*/ CV_RETR_TREE, CV_CHAIN_APPROX_SIMPLE, Point(0, 0) );

	/// Screen out all but interesting contours
	vector<vector<Point> > valid_contours;
	Mat lines = Mat::zeros( thresholded_image.size(), CV_8UC3 );
	for( int i = 0; i< contours.size(); i++ )
	{
		vector<Point> vp = contours[i];
	        Mat pointsf;
        	Mat(vp).convertTo(pointsf, CV_32F);
		Rect boundRect = boundingRect(vp);
		Scalar color = Scalar( 255, 255, 255 );
		drawContours( lines, contours, i, color, 1, 8, hierarchy, 0, Point() );
		if ( vp.size() >= 5 ) {
			RotatedRect fitted_ellipse = fitEllipse(pointsf);
			if ( ( ( fitted_ellipse.angle >= 0 ) && 
			       ( fitted_ellipse.angle < 5 ) ) ||
			     ( ( fitted_ellipse.angle >= 175 ) && 
			       ( fitted_ellipse.angle <= 180) ) ) {

				if ( ( fitted_ellipse.size.height > 40 ) &&
				     ( fitted_ellipse.size.width  > 10 ) ) {

					float ratio_h_to_w = (float)fitted_ellipse.size.width / (float)fitted_ellipse.size.height;
					if ( ( ratio_h_to_w > .1 ) && ( ratio_h_to_w < .75 ) ) { 

						printf("Ellipse Angle:  %f.  Ratio (W/H):  %f\n", fitted_ellipse.angle, ratio_h_to_w);			
						ellipse(lines,fitted_ellipse,Scalar(0,255,0),2);
					}
				}
			}
		}
		valid_contours.push_back(vp);
	}
	imshow("lines", lines);

	return;	

	/// Apply the Hough Transform to find the circles
	cv::HoughCircles( grayscale, circles, CV_HOUGH_GRADIENT, 1, 60,     200, 20, 40, 500 );

	/// Draw the circles detected
	for( size_t i = 0; i < circles.size(); i++ ) 
	{
		Point center(cvRound(circles[i][0]), cvRound(circles[i][1]));
		int radius = cvRound(circles[i][2]);
		cv::circle( grayscale, center, 3, Scalar(0,255,255), -1);
		cv::circle( grayscale, center, radius, Scalar(0,0,255), 1 );
	}
	imshow("edges", grayscale);
}

void process_retroreflective_tape( Mat& frame, Mat& grayscale, Mat& grayscale_blur, Mat& cdst, Mat& edges ) 
{
	Mat ch1, ch2, ch3;
	Mat thresholded_image;

	// "channels" is a vector of 3 Mat arrays:
	vector<Mat> channels(3);
	// split img:
	split(frame, channels);
	// Extract the green channel (retro-reflective type lit up by the Green LED light)
	
	threshold(channels[1], thresholded_image, 250, 255, THRESH_BINARY);
	imshow("edges", thresholded_image);

	vector<vector<Point> > contours;
	vector<Vec4i> hierarchy;

	/// Find contours
	findContours( thresholded_image, contours, hierarchy, CV_RETR_EXTERNAL /* CV_RETR_TREE */, CV_CHAIN_APPROX_SIMPLE, Point(0, 0) );

	/// Screen out all but interesting contours
	vector<vector<Point> > valid_contours;
	Mat lines = Mat::zeros( thresholded_image.size(), CV_8UC3 );
	for( int i = 0; i< contours.size(); i++ )
	{
		vector<Point> vp = contours[i];
		Rect boundRect = boundingRect(vp);
		if(boundRect.height < 25 || boundRect.width < 25){
			continue;
		}
		float aspect = (float)boundRect.width/(float)boundRect.height;
		if(aspect < 1.0) {
			continue;
		}
		Scalar color = Scalar( 255, 255, 255 );
		drawContours( lines, contours, i, color, 2, 8, hierarchy, 0, Point() );
		valid_contours.push_back(vp);
	}
	if ( valid_contours.size() == 1 ) {
		Scalar color = Scalar( 0, 0, 255 );
		drawContours( lines, valid_contours, 0, color, 2, 8, hierarchy, 0, Point() );
	}
	imshow("lines", lines);
}


#define HORIZONTAL_LINE_ANGLE_DEGREES  90.0f
#define VERTICAL_LINE_ANGLE_DEGREES     0.0f

/* The allowable angle range for shield front edges */
#define SHIELD_EDGE_MIN_ANGLE_DEGREES  45.0f
#define SHIELD_EDGE_MAX_ANGLE_DEGREES 135.0f

/* The allowable angle delta for coincident shield front edges */
#define SHIELD_EDGE_ANGLE_MATCH_DELTA_DEGREES 0.5f

/* Max pixel range for coincident shield front edges */
#define SHIELD_EDGE_LINE_INTERSECION_WIDTH_PX 5

/* Allowable range (deviation from vertical) for dividers */
#define DIVIDER_EDGE_VERTICAL_DEV_DEGREES 15.0f

void process_frame_shield_divider( Mat& frame, Mat& grayscale, Mat& grayscale_blur, Mat& cdst, Mat& edges ) 
{
	vector<Vec4i> lines;
	cvtColor(frame, grayscale, COLOR_BGR2GRAY);
	GaussianBlur(grayscale, grayscale_blur, Size(13,13), 1.5, 1.5);
	Canny(grayscale_blur, edges, 20, 80, 3);
	cvtColor(edges, cdst, CV_GRAY2BGR);
	HoughLinesP(edges, lines, 1, CV_PI/180, 50, 50, 20 );

	Point lowest_line_begin(0,0);
	Point lowest_line_end(0,0);
	float lowest_line_angle = 0.0f;
	Vec4i lowest_line_vector;
	int lowest_lower_line_area = INT_MAX;
	Point bottom_left(0, FRAME_HEIGHT_PIXELS-1);
	Point bottom_right(FRAME_WIDTH_PIXELS-1,FRAME_HEIGHT_PIXELS-1);
	for( size_t i = 0; i < lines.size(); i++ )
	{
		Vec4i l = lines[i];
		Point point_begin(l[0], l[1]);
		Point point_end(l[2], l[3]);
		line( cdst, point_begin, point_end, Scalar(0,0,255), 1, CV_AA);
	
		float line_angle = angle_between(point_begin,point_end);
		printf("Line angle:  %f\n",line_angle);
		// As long as the line isn't completely contained within the frame border...
		if (((point_begin.y > BORDER_WIDTH_PIXELS) && (point_end.y > BORDER_WIDTH_PIXELS)) &&
		    ((point_begin.y < (FRAME_HEIGHT_PIXELS-BORDER_WIDTH_PIXELS)) && 
		     (point_end.y < (FRAME_HEIGHT_PIXELS-BORDER_WIDTH_PIXELS)))) {
			if ( ( line_angle != HORIZONTAL_LINE_ANGLE_DEGREES ) &&
			     ( line_angle != VERTICAL_LINE_ANGLE_DEGREES ) ) {

				// Determine if this is the lowest line (smallest area with
				// respect to the bottom edge of the image) and with the
				// valid range for a shield edge.
				Point projected_line_begin;
				Point projected_line_end;
				project_line( point_begin, point_end,
			      		      projected_line_begin,
			      		      projected_line_end);
				int low_area = rectangle_area( projected_line_begin,
									projected_line_end,
									bottom_left,
									bottom_right);
				printf("Beg: %i, %i End: %i, %i  Area:  %i\n",projected_line_begin.x, projected_line_begin.y, projected_line_end.x, projected_line_end.y,low_area);
				if ( low_area < lowest_lower_line_area ) {
					lowest_lower_line_area = low_area;
					if ( ( line_angle >= SHIELD_EDGE_MIN_ANGLE_DEGREES ) &&
					     ( line_angle <= SHIELD_EDGE_MAX_ANGLE_DEGREES ) ) {
						lowest_line_begin = point_begin;
						lowest_line_end = point_end;
						lowest_line_angle = line_angle;
						lowest_line_vector = l;
					}
				}
			}
		}
	}
	std::list<Vec4i> shield_edge_lines;
	// Find those lines within 1 degree of the same angle as the lowest line
	if ( lowest_line_angle > 0.0f ) {
		shield_edge_lines.push_back(lowest_line_vector);
		Point projected_lowest_line_begin;
		Point projected_lowest_line_end;
		project_line( lowest_line_begin, lowest_line_end,
			      projected_lowest_line_begin,
			      projected_lowest_line_end);
		line( cdst, projected_lowest_line_begin, projected_lowest_line_end, Scalar(128,0,128), 1, CV_AA);
		for( size_t i = 0; i < lines.size(); i++ )
		{
			Vec4i l = lines[i];
			Point point_begin(l[0], l[1]);
			Point point_end(l[2], l[3]);
			float line_angle = angle_between(point_begin,point_end);
			if (fabs(line_angle-lowest_line_angle) <= SHIELD_EDGE_ANGLE_MATCH_DELTA_DEGREES) {
				Point projected_line_begin;
				Point projected_line_end;
				project_line( point_begin, point_end,
			      		      projected_line_begin,
			      		      projected_line_end);
				if ((projected_line_begin.y > 
					(projected_lowest_line_begin.y - 
						SHIELD_EDGE_LINE_INTERSECION_WIDTH_PX)) && 
				    (projected_line_begin.y < 
					(projected_lowest_line_begin.y +
						SHIELD_EDGE_LINE_INTERSECION_WIDTH_PX))) {
					line( cdst, point_begin, point_end, Scalar(0,128,0), 2, CV_AA);
					shield_edge_lines.push_back(l);
				}
			}
		}
		/* Find those lines which are near verical,
		   and which intersect any of the "lowest lines" */
		for( size_t i = 0; i < lines.size(); i++ )
		{
			Vec4i l = lines[i];https://www.youtube.com/watch?v=f67tl7OLAjo
			Point point_begin(l[0], l[1]);
			Point point_end(l[2], l[3]);
			float line_angle = angle_between(point_begin,point_end);			
			if ((( fabs(line_angle) > (360.0f - DIVIDER_EDGE_VERTICAL_DEV_DEGREES)) ||
			     ( fabs(line_angle) < (0.0f + DIVIDER_EDGE_VERTICAL_DEV_DEGREES))) ||
			    (( fabs(line_angle) > (180.0f - DIVIDER_EDGE_VERTICAL_DEV_DEGREES)) ||
			     ( fabs(line_angle) < (0.0f + DIVIDER_EDGE_VERTICAL_DEV_DEGREES))))
 {
				/* Possible vertical line */
				/* Detect a line as a shield edge if 
                                   it's beginning/end point intersects any of the
				   lowest line segments.  Higher confidence if two are 
				   found parallel to each other. */
				std::list<Vec4i>::const_iterator iterator;
				printf("Shield Edge Lines:  %i\n", shield_edge_lines.size());
				for (iterator = shield_edge_lines.begin(); iterator != shield_edge_lines.end(); ++iterator) {
					Vec4i l = *iterator;
					if (point_on_line( Point(l[0],l[1]), Point(l[2],l[3]), point_begin,400)) {
						/* Only include line if it ascends from this point */
						if ( point_end.y < point_begin.y ) {
							line( cdst, point_begin, point_end, Scalar(0,128,128), 2, CV_AA);
							circle( cdst, point_begin, 11, Scalar(0,128,0), 1, CV_AA);
						}
					} else if (point_on_line( Point(l[0],l[1]), Point(l[2],l[3]), point_end,400)) {
						/* Only include line if it ascends from this point */
						if ( point_end.y > point_begin.y ) {
							line( cdst, point_begin, point_end, Scalar(0,128,128), 2, CV_AA);
							circle( cdst, point_end, 11, Scalar(0,128,0), 1, CV_AA);
						}
					}
				}
			}
		}
	}
	if ( lowest_line_begin.y > 0 ) {
		line( cdst, lowest_line_begin, lowest_line_end, Scalar(255,0,0), 3, CV_AA);
		float line_angle = angle_between(lowest_line_begin,lowest_line_end);
		printf("lowest Line angle:  %f\n",line_angle);
		printf("lowest line:  Begin:  %i, %i.  End:  %i, %i\n",
			lowest_line_begin.x,
			lowest_line_begin.y,
			lowest_line_end.x,
			lowest_line_end.y);
	}
	imshow("edges", edges);
	imshow("lines", cdst);
}

int process_circles(Mat& src, Mat& grayscale, Mat& cdst)
{
  /// Convert it to gray
  cvtColor( src, grayscale, CV_BGR2GRAY );

  /// Reduce the noise so we avoid false circle detection
  GaussianBlur( grayscale, grayscale, Size(11, 11), 1.5, 1.5 );

  vector<Vec3f> circles;

  int circle_count = 0;
  Canny(grayscale, src, 20, 80, 3);

  /// Apply the Hough Transform to find the circles
  HoughCircles( src, circles, CV_HOUGH_GRADIENT, 1, 30, 200, 50); //grayscale.rows/4, 200, 100/*, 80, 40, 0, 0*/ );

  cvtColor(src, src, CV_GRAY2BGR);

  //src = cv::Scalar(0,0,0);  /// Draw the circles detected
  for( size_t i = 0; i < circles.size(); i++ )
  {
      Point center(cvRound(circles[i][0]), cvRound(circles[i][1]));
      int radius = cvRound(circles[i][2]);
      // circle center
      circle( src, center, 3, Scalar(0,255,0), -1, 8, 0 );
      // circle outline
      circle( src, center, radius, Scalar(0,0,255), 3, 8, 0 );
      circle_count++;
   }
   printf("Detected %i circles.\n",circle_count);

  /// Show your results
  namedWindow( "Hough Circle Transform Demo", CV_WINDOW_AUTOSIZE );
  imshow( "Hough Circle Transform Demo", src );

  waitKey(0);
  return 0;
}
