#include "opencv2/opencv.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include <stdio.h>
#include <math.h>
#include <iostream>
#include <string>
#include <climits>
#include <errno.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/param.h>
#include <netdb.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <fstream>
#include <chrono>
#include <signal.h>
#include "SafeQueue.h"
#include <dirent.h>
#include <errno.h>
#include <sstream>
#include <cstdio>
#include <thread>
#include "ntcore.h"
#include "networktables/NetworkTable.h"
#include <math.h>

using namespace cv;

typedef struct {
    bool enable_algorithm;
    bool enable_stream_out;
    bool enable_file_out;
    int  algorithm;
    int  input_camera;
    double algorithm_param1;
    double algorithm_param2;    
} videoproc_settings;

typedef struct {
    bool   algorithm_active;
    int    last_algorithm_time_us;
    int    last_read_time_us;
    int    last_write_time_us;
} algorithm_stats;

typedef struct {
    bool   target_detected;
    double target_distance_inches;
    double target_angle_degrees;
    double snr;
    int    successive_detection_count;
} algorithm_results;

void process_frame_shield_divider( Mat& frame, Mat& grayscale, Mat& grayscale_blur, Mat& cdst, Mat& edges, algorithm_results& results );
void process_retroreflective_tape( Mat& frame, Mat& grayscale, Mat& grayscale_blur, Mat& cdst, Mat& edges, algorithm_results& results );
void process_tower_openings( Mat& frame, Mat& grayscale, Mat& grayscale_blur, Mat& cdst, Mat& edges, algorithm_results& results );
int process_circles(Mat& src, Mat& grayscale, Mat& cdst, algorithm_results& results);
void process_tower_lights_red( Mat& frame, Mat& grayscale, Mat& grayscale_blur, Mat& cdst, Mat& edges, algorithm_results& results );
void process_tower_lights_blue( Mat& frame, Mat& grayscale, Mat& grayscale_blur, Mat& cdst, Mat& edges, algorithm_results& results );
void process_tower_lights( Mat& frame, Mat& grayscale, Mat& grayscale_blur, Mat& cdst, Mat& edges, algorithm_results& results, bool blue ); 

#define ALGORITHM_SHIELD_DIVIDER               1
#define ALGORITHM_TOWER_RETROREFLECTIVE_TAPE   2
#define ALGORITHM_TOWER_OPENING                3
#define ALGORITHM_TOWER_LIGHTS_BLUE            4
#define ALGORITHM_TOWER_LIGHTS_RED             5
#define ALGORITHM_FIRST			               1
#define ALGORITHM_LAST                         ALGORITHM_TOWER_LIGHTS_RED			      

std::mutex alg_stats_mutex;
algorithm_stats curr_alg_stats;

std::mutex alg_results_mutex;
algorithm_results curr_alg_results;

void init_algorithm_stats( algorithm_stats& stats ) 
{
    stats.algorithm_active = false;
    stats.last_algorithm_time_us = 0;
    stats.last_read_time_us = 0;
    stats.last_write_time_us = 0;    
}

void get_algorithm_stats( algorithm_stats& stats )
{
    std::lock_guard<std::mutex> lock (alg_stats_mutex);
    stats = curr_alg_stats;
}

void set_algorithm_stats( algorithm_stats& stats )
{
    std::lock_guard<std::mutex> lock (alg_stats_mutex);
    curr_alg_stats = stats;
}

void init_algorithm_results( algorithm_results& results ) 
{
    results.target_detected = false;
    results.target_distance_inches = 0;
    results.target_angle_degrees = 0;
    results.snr = 0;
    results.successive_detection_count = 0;    
}

void get_algorithm_results( algorithm_results& results )
{
    std::lock_guard<std::mutex> lock (alg_results_mutex);
    results = curr_alg_results;
}

void set_algorithm_results( algorithm_results& results )
{
    std::lock_guard<std::mutex> lock (alg_results_mutex);
    curr_alg_results = results;
}

int camera_main(char *video_file, videoproc_settings& settings);
int image_main(char *img, int algorithm);

#define printf(...)

const char *output_video_dir_root = "/media/ubuntu";
const char *output_video_filename_prefix = "video_capture_";
const char *output_video_file_extension = ".avi";

const char *output_mjpeg_streamer_dir = "streamer";
const char *output_mjpeg_streamer_filename = "current.jpg";

bool get_first_in_dir(std::string& first_dir)
{
   DIR *dp;
   struct dirent *dirp;
   dp = opendir(output_video_dir_root);
   if (dp == NULL) {
       std::cout << "Error opening video out dir " << output_video_dir_root << "Errno:  " << errno << std::endl;
       return false;
   }
   while ((dirp = readdir(dp)) != NULL ) {
       if ((strcmp(".", dirp->d_name) != 0) &&
           (strcmp("..", dirp->d_name) != 0)) {
           first_dir = output_video_dir_root;
           first_dir += "/";
           first_dir += dirp->d_name;
           first_dir += "/";
           std::cout << "Found first entry:  " << first_dir << std::endl;
           return true;
       }
   }
}

bool write_cam_data_to_file = true;

bool find_next_output_video_file_name( std::string& output ) 
{
    char filename[1024];
    struct stat file_attributes;
    std::string output_dir;
    get_first_in_dir(output_dir);
    std::string output_file_prefix = output_dir + output_video_filename_prefix;
    for ( int i = 0; i < 1000; i++ ) {
        sprintf(filename,"%s%d%s", output_file_prefix.c_str(),
                i, output_video_file_extension);
        std::ifstream file_stream(filename);
        if ( !file_stream ) {
            /* File does not exist */
            output = filename;
            return true;
        }
    }
    return false;
}

bool get_mjpg_streamer_dir( std::string& dir, bool& created )
{
    std::string output_dir;
    get_first_in_dir(output_dir);
    // Create directory for mjpeg-streamer
    dir = output_dir;
    dir += output_mjpeg_streamer_dir;
    int result = mkdir(dir.c_str(), S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH );
    if ( result == 0 ) {
        created = true;
        return true;
    } else {
        if ( errno == EEXIST ) {
            created = false;
            return true;
        }
    }
    return false;
}

bool file_exists( std::string& file_path )
{
    FILE *fp = fopen(file_path.c_str(),"r");
    if ( fp ) {
        fclose(fp);
        return true;
    } else {
        return false;
    }
}

int kbhit(void)
{
        struct timeval tv;  fd_set
        read_fd;  /* Do not wait at all, not even a microsecond */
        tv.tv_sec=0;
        tv.tv_usec=0;  /* Must be done first to initialize read_fd */
        FD_ZERO(&read_fd);  /* Makes select() ask if input is ready:   *
                               0 is the file descriptor for stdin      */
        FD_SET(0,&read_fd);  /* The first parameter is the number of the *
                                largest file descriptor to check + 1. */
        if(select(1, &read_fd, NULL, /*No writes*/ NULL, /*No exceptions*/
&tv) == -1)
                return 0; /* An error occured */

          /* read_fd now holds a bit map of files that are   *
           readable. We test the entry for the standard    *
           input (file 0). */
          if(FD_ISSET(0,&read_fd))    /* Character pending on stdin */
                  return 1;  /* no characters were pending */
        return 0;
}

bool quit_algorithm = false;
bool quit_application = false;
bool quit_networktables = false;
bool settings_change = false;

void sigproc(int sig);

/* sigproc() - invoked when ctrl-c is pressed. */
void sigproc(int sig)
{
    quit_algorithm = true;
    quit_application = true;
    quit_networktables = true;    
}

void init_videoproc_settings(videoproc_settings& settings) {
    settings.enable_algorithm = false;
    settings.enable_stream_out = false;
    settings.enable_file_out = false;
    settings.algorithm = 999;
    settings.input_camera = 999;
    settings.algorithm_param1 = 999.0;
    settings.algorithm_param2 = 999.0;
}

bool compare_settings(videoproc_settings& settings1, videoproc_settings& settings2) {
    if ( ( settings1.enable_algorithm != settings2.enable_algorithm ) ||
         ( settings1.enable_stream_out != settings2.enable_stream_out ) ||
         ( settings1.enable_file_out != settings2.enable_file_out ) ||
         ( settings1.algorithm != settings2.algorithm ) ||
         ( settings1.input_camera != settings2.input_camera ) ||         
         ( settings1.algorithm_param1 != settings2.algorithm_param1 ) ||
         ( settings1.algorithm_param2 != settings2.algorithm_param2 ) ) {
         return false;
    }
    return true;
}

videoproc_settings curr_settings;
  
void run_under_remote_control (char *server_ip_address) 
{
  std::cout << "Running under remote control.  ServerIP:  " << server_ip_address << std::endl;
  auto nt = NetworkTable::GetTable("videoproc");
  
  nt->SetClientMode();
  hostent * record = gethostbyname(server_ip_address);
  if ( record == NULL ) {
      std::cout << "Unable to retrieve ip address for: " << server_ip_address << std::endl;
      quit_networktables = true;
      return;
  }
  in_addr *address = (in_addr *)record->h_addr;
  std::string ip_addr = inet_ntoa(*address);
  ip_addr += "\n"; /* ??? */
  nt->SetIPAddress(ip_addr.c_str());
  
  nt->Initialize();
  std::this_thread::sleep_for(std::chrono::seconds(5));

  videoproc_settings new_settings;
  init_videoproc_settings(new_settings);

  while (!quit_networktables) {     
    
    algorithm_stats alg_stats;
    get_algorithm_stats(alg_stats);
    algorithm_results alg_results;
    get_algorithm_results(alg_results);
    
    nt->PutBoolean("algorithm_active", alg_stats.algorithm_active);
    nt->PutNumber("last_algorithm_time_us", alg_stats.last_algorithm_time_us);
    nt->PutNumber("last_read_time_us", alg_stats.last_read_time_us);
    nt->PutNumber("last_write_time_us", alg_stats.last_write_time_us);

    nt->PutNumber("target_distance_inches", alg_results.target_distance_inches);
    nt->PutNumber("target_angle_degrees", alg_results.target_angle_degrees);
    nt->PutBoolean("target_detected", alg_results.target_detected);
    nt->PutNumber("snr", alg_results.snr);
    nt->PutNumber("successive_detection_count", alg_results.successive_detection_count);
    
    if ( nt->ContainsKey("enable_algorithm") ) {
       new_settings.enable_algorithm = nt->GetBoolean("enable_algorithm",false);
       //std::cout << "enable_algorithm:  " << new_settings.enable_algorithm << std::endl;
    }
    if ( nt->ContainsKey("enable_stream_out") ) {
       new_settings.enable_stream_out = nt->GetBoolean("enable_stream_out",false);
       //std::cout << "enable_stream_out:  " << new_settings.enable_stream_out << std::endl;
    }  
    if ( nt->ContainsKey("enable_file_out") ) {
       new_settings.enable_file_out = nt->GetBoolean("enable_file_out",false);
       //std::cout << "enable_file_out:  " << new_settings.enable_file_out << std::endl;
    }  
    if ( nt->ContainsKey("algorithm") ) {
       new_settings.algorithm = (int)nt->GetNumber("algorithm",999);
       //std::cout << "algorithm:  " << new_settings.algorithm << std::endl;
    }
    if ( nt->ContainsKey("input_camera") ) {
       new_settings.input_camera = (int)nt->GetNumber("input_camera",999);
       //std::cout << "input_camera:  " << new_settings.input_camera << std::endl;
    } 
    if ( nt->ContainsKey("algorithm_param1") ) {
       new_settings.algorithm_param1 = nt->GetNumber("algorithm_param1",999);
       //std::cout << "algorithm_param1:  " << new_settings.algorithm_param1 << std::endl;
    }    
    if ( nt->ContainsKey("algorithm_param2") ) {
       new_settings.algorithm_param2 = nt->GetNumber("algorithm_param2",999);
       //std::cout << "algorithm_param2:  " << new_settings.algorithm_param2 << std::endl;
    }        
    if ( nt->ContainsKey("ping") ) {
       double ping_value = nt->GetNumber("ping",-1.0);
       //std::cout << "ping:  " << ping_value << std::endl;
       nt->PutNumber("ping_response",ping_value);
    }    
    
    if ( !compare_settings( curr_settings, new_settings ) ) {
        std::cout << "SETTINGS CHANGED!" << std::endl;
        curr_settings = new_settings;
        settings_change = true;
    }
           
    std::this_thread::sleep_for(std::chrono::milliseconds(200));
  }
}

VideoCapture *cam0 = NULL;
VideoCapture *cam1 = NULL;

int main(int argc, char** argv)
{
    std::cout << "Starting detector." << std::endl;
	if ( argc < 2 ) {
		std::cout << "Parameters:  <ip_address or algorithm #[1-3]> <filename/camera_number>" << std::endl;
	} else {
	    std::cout << "argv[1]: " << argv[1] << std::endl;
	}
	
    init_videoproc_settings(curr_settings);
    init_algorithm_stats(curr_alg_stats);
    init_algorithm_results(curr_alg_results);
    
	char *ip_address = NULL;
	std::istringstream convert(argv[1]);
	if ( strlen(argv[1]) > 2 ) {
	    ip_address = argv[1];
	    std::cout << "IP Address:  " << ip_address << std::endl;
	    /* Run in remote control mode, using the ip_address as the NetworkTables server. */
	    std::thread t1(run_under_remote_control,ip_address);
	    
	    while (!quit_application) {
	         /* Run the algorithm based upon the current settings */
	         if ( settings_change ) {
	             videoproc_settings settings_copy = curr_settings;
	             settings_change = false;
	             if ( settings_copy.enable_algorithm ) {
	                 std::cout << "Invoking camera_main() with new settings." << std::endl;
                     camera_main(NULL, settings_copy);
                     /* This will return when either application exit has been requested,*/
                     /* or when the current settings have been changed by the remote     */
                     /* controller.                                                      */	         
                 }
	         } else {
	             std::this_thread::sleep_for(std::chrono::milliseconds(25));
	         }
	    }
	    
	    t1.join();
	    
	} else { 
	    int algorithm;
	    convert >> algorithm;
	    std::cout << "Running in manual (command-line) mode.  Algorithm:  " << algorithm << std::endl;
	    if ( ( algorithm < ALGORITHM_FIRST ) ||
	         ( algorithm > ALGORITHM_LAST ) ) {
		    std::cout << "Invalid algorithm number." << std::endl;
		    return -2;
	    }
	    curr_settings.algorithm = algorithm;
	    curr_settings.enable_algorithm = true;
	    curr_settings.enable_file_out = true;
	    curr_settings.enable_stream_out = true;
        curr_settings.algorithm_param1 = 999.0;
        curr_settings.algorithm_param2 = 999.0;	    	    
        signal(SIGINT,sigproc);
	    if ( argc > 2 ) {
		    std::string arg(argv[2]);
		    if( (arg.substr(arg.find_last_of(".") + 1) == "mp4") ||
		        (arg.substr(arg.find_last_of(".") + 1) == "avi") ) {
			    std::cout << "Running camera_main() with file " << arg << std::endl;
			    camera_main(argv[2], curr_settings);
			} else if ((arg == "0") ||
                       (arg == "1")) {
                curr_settings.input_camera = atoi(arg.c_str());
                camera_main(NULL, curr_settings);
		    } else {
			    image_main(argv[2], algorithm);
		    }
	    }
	}
	if ( cam0 != NULL ) {
	    delete cam0;
	}
	if ( cam1 != NULL ) {
	    delete cam1;
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

    algorithm_results alg_results;
    init_algorithm_results(alg_results);
	Size size(640,480);//the dst image size
	resize(inframe,frame,size);//resize image
	if ( algorithm == ALGORITHM_SHIELD_DIVIDER ) {
		process_frame_shield_divider(frame, grayscale, grayscale_blur, cdst, edges, alg_results);
	} else if ( algorithm == ALGORITHM_TOWER_RETROREFLECTIVE_TAPE ) {
		process_retroreflective_tape(frame, grayscale, grayscale_blur, cdst, edges, alg_results);
	} else if ( algorithm == ALGORITHM_TOWER_OPENING ) {
		process_tower_openings(frame, grayscale, grayscale_blur, cdst, edges, alg_results);
	} else if ( algorithm == ALGORITHM_TOWER_LIGHTS_RED ) {
		process_tower_lights_red(frame, grayscale, grayscale_blur, cdst, edges, alg_results);
	} else if ( algorithm == ALGORITHM_TOWER_LIGHTS_BLUE ) {
		process_tower_lights_blue(frame, grayscale, grayscale_blur, cdst, edges, alg_results);
	}
	 
	//process_circles(frame, grayscale, cdst, alg_results);	
	waitKey(0);

	return -1;
}

int camera_main(char *video_file, videoproc_settings& settings)
{
    bool live_camera_source = true;
    printf("camera_main()\n");
    VideoCapture *cap;
    VideoWriter *writer = NULL;
    VideoWriter *still_writer = NULL; /* Output MJPG File for reading by mjpg-streamer */
    if ( video_file != NULL ) {
        cap = new VideoCapture(video_file);
        std::cout << "Opened VideoCapture on " << video_file << std::endl;
        live_camera_source = false;
    } else {
        if ( settings.input_camera == 0 ) {
            if ( cam0 == NULL ) {
                std::cout << "Opening camera " << settings.input_camera << std::endl;
                cam0 = new VideoCapture(0);
                printf("Opened camera.\n");
                }
            cap = cam0;
        } else if ( settings.input_camera == 1 ) {
            if ( cam1 == NULL ) {
                std::cout << "Opening camera " << settings.input_camera << std::endl;            
                cam1 = new VideoCapture(1);
                printf("Opened camera.\n");
            }
            cap = cam1;
        } 
    }
    printf("VideoCapture instantiated.\n");
    if(!cap->isOpened())  // check if we succeeded
        return -1;

    printf("Frame Width:  %i\n", (int)cap->get(CV_CAP_PROP_FRAME_WIDTH));
    printf("Frame Height:  %i\n", (int)cap->get(CV_CAP_PROP_FRAME_HEIGHT));
    printf("FPS:  %i\n", (int)cap->get(CV_CAP_PROP_FPS));

    Size size(640,480);//the dst image size
    std::string mjpg_streamer_dir;
    bool mjpg_streamer_dir_created;
    std::string mjpg_streamer_file;
    bool mjpg_streamer_output = false;
    if ( live_camera_source ) {
        string output_filename;
        if ( settings.enable_file_out && find_next_output_video_file_name( output_filename ) ) {
            bool color = true;
            writer = new VideoWriter( output_filename, 0/*CV_FOURCC('M','J','P','G')*/, 29.997, size, color);
        } else {
            printf("Unable to find available video output file name.\n");
        }
        if ( settings.enable_stream_out && get_mjpg_streamer_dir( mjpg_streamer_dir, mjpg_streamer_dir_created ) ) {
            std::cout << "mjpg streamer dir:  " << mjpg_streamer_dir << " Created:  " << mjpg_streamer_dir_created << std::endl;
            mjpg_streamer_file = mjpg_streamer_dir;
            mjpg_streamer_file += "/";
            mjpg_streamer_file += output_mjpeg_streamer_filename;
            mjpg_streamer_output = true;
            std::cout << "Outputting to mjpg streamer file:  " << mjpg_streamer_file << std::endl;
        } else {
            std::cout << "Error retrieving mjpg streamer dir" << std::endl;
        }    
    }
    
    vector<int> compression_params; 
    compression_params.push_back(CV_IMWRITE_JPEG_QUALITY);
    compression_params.push_back(60);

    algorithm_stats alg_stats;
    init_algorithm_stats(alg_stats);
    alg_stats.algorithm_active = true;

    algorithm_results alg_results;
    init_algorithm_results(alg_results);

    Mat edges;
    Mat grayscale;
    Mat grayscale_blur;
    Mat cdst;
    namedWindow("edges",1);
    namedWindow("lines",1);
    startWindowThread();
    std::cout << "camera_main() initialization complete." << std::endl;
    while(!settings_change && !quit_algorithm)
    {
        Mat inframe;
	    Mat frame;
        printf("Starting processing loop.\n");
        std::chrono::steady_clock::time_point begin = std::chrono::steady_clock::now();
        printf("Acquiring frame...\n");
        if ( video_file == NULL ) {
            printf("Reading from camera\n");
            *cap >> frame;
        } else {
            printf("Reading frame...");
            *cap >> inframe;
            printf("read from file complete....");
            resize(inframe,frame,size);
            printf("Resize complete.\n");
        }
        std::chrono::steady_clock::time_point end = std::chrono::steady_clock::now();
        alg_stats.last_read_time_us = std::chrono::duration_cast<std::chrono::microseconds>(end - begin).count();
        std::cout << "Camera Read (us):  " << alg_stats.last_read_time_us << std::endl;

        std::chrono::steady_clock::time_point algo_begin = std::chrono::steady_clock::now();
	    if ( settings.algorithm == ALGORITHM_SHIELD_DIVIDER ) {
		    process_frame_shield_divider(frame, grayscale, grayscale_blur, cdst, edges, alg_results);
	    } else if ( settings.algorithm == ALGORITHM_TOWER_RETROREFLECTIVE_TAPE ) {
		   process_retroreflective_tape(frame, grayscale, grayscale_blur, cdst, edges, alg_results);
	    } else if ( settings.algorithm == ALGORITHM_TOWER_OPENING ) {
		    process_tower_openings(frame, grayscale, grayscale_blur, cdst, edges, alg_results);
	    } else if ( settings.algorithm == ALGORITHM_TOWER_LIGHTS_RED ) {
		    process_tower_lights_red(frame, grayscale, grayscale_blur, cdst, edges, alg_results);
	    } else if ( settings.algorithm == ALGORITHM_TOWER_LIGHTS_BLUE ) {
		    process_tower_lights_blue(frame, grayscale, grayscale_blur, cdst, edges, alg_results);
	    }	    
	    set_algorithm_results(alg_results);
        std::chrono::steady_clock::time_point algo_end = std::chrono::steady_clock::now();
        alg_stats.last_algorithm_time_us = std::chrono::duration_cast<std::chrono::microseconds>(algo_end - algo_begin).count();
        std::cout << "Algorithm (us):  " << alg_stats.last_algorithm_time_us << std::endl;
        
        if ( writer || mjpg_streamer_output ) {
            std::chrono::steady_clock::time_point begin = std::chrono::steady_clock::now();
            if ( writer ) {
                writer->write(frame);
            }
            if ( mjpg_streamer_output ) {
                if ( !file_exists( mjpg_streamer_file ) ) {
                    imwrite(mjpg_streamer_file,frame,compression_params);
                }
            }
            std::chrono::steady_clock::time_point end = std::chrono::steady_clock::now();
            alg_stats.last_write_time_us = std::chrono::duration_cast<std::chrono::microseconds>(end - begin).count();
            std::cout << "Frame Write (us):  " << alg_stats.last_write_time_us << std::endl;
         }
        std::chrono::steady_clock::time_point wait_begin = std::chrono::steady_clock::now();
        set_algorithm_stats(alg_stats);
    }
    init_algorithm_stats(alg_stats);
    set_algorithm_stats(alg_stats);
    init_algorithm_results(alg_results);
    set_algorithm_results(alg_results);
    std::cout << "Cleaning up camera_main()" << settings_change << " " << quit_algorithm << std::endl;
    if ( writer ) {
        delete writer;
    }
    destroyAllWindows();
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

void new_point_at_angle(const Point &v1, float angle, int length, Point& out)
{
    angle += 90;
    if (angle >= 360) {
        angle -= 360;
    }
    double angle_radians = angle * CV_PI / 180.0;
    out.x = (int)round(v1.x + length * cos(angle_radians));
    out.y = (int)round(v1.y + length * sin(angle_radians));
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
	    printf("Inf slope.\n");
		if ( v1.x == v2.x ) {
		    out_begin.x = v1.x;
			out_begin.y = 0;
			out_end.x = v2.x;
			out_end.y = FRAME_HEIGHT_PIXELS - 1;
		} else if ( v1.y == v2.y ) {
			out_begin.x = 0;
			out_begin.y = v1.y;
			out_end.x = FRAME_WIDTH_PIXELS - 1;
			out_end.y = v2.y;
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
	printf("out1:  %i,%i, out2:  %i,%i\n",
		out_begin.x, out_begin.y,
		out_end.x, out_end.y);	
}

void project_line_at_angle(const Point &v1, float angle, Point &out1, Point &out2)
{
    Point v2;
    new_point_at_angle(v1, angle, FRAME_WIDTH_PIXELS, v2);
    project_line(v1, v2, out1, out2);
}

bool point_in_polygon( const Point& pt, Point* points, int num_points)
{
    int i, j, nvert = num_points;
    bool c = false;
    for (i = 0, j = nvert - 1; i < nvert; j = i++) {
        if( ( (points[i].y >= pt.y) != (points[j].y >= pt.y) ) &&
            (pt.x <= (points[j].x - points[i].x) * 
                (pt.y - points[i].y) / (points[j].y - points[i].y) + points[i].x))
            c = !c;
    } 
}

typedef struct {
    Point box[4];
    Point centerline_begin;
    Point centerline_end;
} enclosing_polygon;

bool project_pt_and_angle_to_square(const Point &v1, float angle, int rect_width_degrees, enclosing_polygon& out) 
{
    /* 1) Determine line along angle which passes through point and extends across frame. */
    project_line_at_angle(v1, angle, out.centerline_begin, out.centerline_end);
    Point2f start(out.centerline_begin.x, out.centerline_begin.y);
    Point2f end(out.centerline_end.x, out.centerline_end.y);
    Point2f midpoint = (start + end)*.5;
    Point midpoint_i( (int)midpoint.x, (int)midpoint.y);
    printf("Initial Point:  %d, %d\n", v1.x, v1.y);
    printf("Angle:  %f, Start:  %d, %d  End:  %d, %d  Midpoint:  %d, %d\n",angle, out.centerline_begin.x, out.centerline_begin.y, out.centerline_end.x, out.centerline_end.y, midpoint_i.x, midpoint_i.y);
    /* 2) Determine line along angle-width, angle+width which passes through point */
    Point v2;
    Point lesser_angle_line_begin;
    Point lesser_angle_line_end;
    float lesser_angle = angle - rect_width_degrees;
    if ( lesser_angle < 0 ) {
        lesser_angle += 360;
    }
    new_point_at_angle(midpoint_i, lesser_angle, FRAME_WIDTH_PIXELS, v2);
    project_line(midpoint_i, v2, lesser_angle_line_begin, lesser_angle_line_end);   
    /* 3) Determine line along angle+width, angle-width which passes through point */
    Point greater_angle_line_begin;
    Point greater_angle_line_end;
    float greater_angle = angle + rect_width_degrees;
    if ( greater_angle >= 360 ) {
        greater_angle -= 360;
    }
    new_point_at_angle(midpoint_i, greater_angle, FRAME_WIDTH_PIXELS, v2);
    project_line(midpoint_i, v2, greater_angle_line_begin, greater_angle_line_end);
    /* 3) Create rect which fully encompasses 2) */
    out.box[0] = lesser_angle_line_begin;
    out.box[1] = greater_angle_line_begin;
    out.box[2] = lesser_angle_line_end;
    out.box[3] = greater_angle_line_end;
    printf("Rect Points:  %d, %d, %d, %d, %d, %d, %d, %d\n", out.box[0].x, 
                                                             out.box[0].y,    
                                                             out.box[1].x,    
                                                             out.box[1].y,
                                                             out.box[2].x,    
                                                             out.box[2].y,    
                                                             out.box[3].x,    
                                                             out.box[3].y);

    bool pt_in_polygon = point_in_polygon(v1,out.box,sizeof(out.box)/sizeof(out.box[0]));
    printf("Point in Polygon:  %s\n",  pt_in_polygon ? "true" : "false");

    return true;
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

void process_tower_openings( Mat& frame, Mat& grayscale, Mat& grayscale_blur, Mat& cdst, Mat& edges, algorithm_results& results )
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

void process_retroreflective_tape( Mat& frame, Mat& grayscale, Mat& grayscale_blur, Mat& cdst, Mat& edges, algorithm_results& results ) 
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

void process_tower_lights_red( Mat& frame, Mat& grayscale, Mat& grayscale_blur, Mat& cdst, Mat& edges, algorithm_results& results ) {
    bool blue = false;
    process_tower_lights( frame, grayscale, grayscale_blur, cdst, edges, results, blue ); 
} 

void process_tower_lights_blue( Mat& frame, Mat& grayscale, Mat& grayscale_blur, Mat& cdst, Mat& edges, algorithm_results& results ) {
    bool blue = true;
    process_tower_lights( frame, grayscale, grayscale_blur, cdst, edges, results, blue );
}

typedef struct {
    Point pt;
    Rect bounding_rect;
} interesting_contour;

#define VERTICAL_LIGHTS_ANGLE_RANGE 10
#define HORIZONTAL_LIGHTS_ANGLE_RANGE 30

#define COLLINEAR_ANGLE_RANGE   1

#define MIN_COLLINEAR_POINTS 4

void process_tower_lights( Mat& frame, Mat& grayscale, Mat& grayscale_blur, Mat& cdst, Mat& edges, algorithm_results& results, bool blue ) 
{
	Mat thresholded_lights_image;
	Mat thresholded_target_image;
	vector<Mat> channels(3);
	
	// Split the RGB image into separate color channel
	split(frame, channels);
	
	// Extract the appropriate color channels	
	int lights_low_threshold = 250;
	int lights_channel = 1;
	int target_channel = 1; /* Always use Green Channel for detecting Target */
	int target_low_threshold = 250;
	if ( blue == false ) {
	     lights_low_threshold = 249;
	     lights_channel = 2; /* Use Red Channel for Red Lights */
	}
    
    GaussianBlur(channels[lights_channel], thresholded_lights_image, Size(7,7), 0.5, 0.5);
	threshold(channels[lights_channel], thresholded_lights_image, lights_low_threshold, 255, THRESH_BINARY);	
    threshold(channels[target_channel], thresholded_target_image, target_low_threshold,
        255, THRESH_BINARY);
	imshow("edges", thresholded_lights_image);

	vector<vector<Point> > contours;
	vector<Vec4i> hierarchy;

 	vector<vector<Point> > target_contours;
	vector<Vec4i> target_hierarchy;

	/// Find contours
	findContours( thresholded_lights_image, contours, hierarchy, CV_RETR_EXTERNAL /* CV_RETR_TREE */, /*CV_CHAIN_APPROX_SIMPLE*//*CV_CHAIN_APPROX_TC89_KCOS*/CV_CHAIN_APPROX_NONE, Point(0, 0) );

    findContours( thresholded_target_image, target_contours, target_hierarchy, CV_RETR_EXTERNAL,
        /*CV_CHAIN_APPROX_SIMPLE*/CV_CHAIN_APPROX_NONE, Point(0,0));

    /// Detect Retroreflective Target
	/// Screen out all but interesting contours
	vector<vector<Point> > valid_target_contours;
	Mat lines = Mat::zeros( thresholded_target_image.size(), CV_8UC3 );

	for( int i = 0; i< target_contours.size(); i++ )
	{
		vector<Point> vp = target_contours[i];
		Rect boundRect = boundingRect(vp);
		if(boundRect.height < 22 || boundRect.width < 30){
			continue;
		}
		float aspect = (float)boundRect.width/(float)boundRect.height;
		if(aspect < 1.0) {
			continue;
		}
		Scalar color = Scalar( 255, 255, 255 );
		drawContours( lines, target_contours, i, color, 2, 8, hierarchy, 0, Point() );
		valid_target_contours.push_back(vp);
	}
	if ( valid_target_contours.size() > 0 ) {
		int largest_contour_area = -1;
		int largest_contour_area_index = -1;
		for ( int i = 0; i < valid_target_contours.size(); i++ ) {
		    Rect boundRect = boundingRect(valid_target_contours[i]);
		    if ( (int)boundRect.area() > largest_contour_area ) {
		        largest_contour_area = (int)boundRect.area();
		        largest_contour_area_index = i;
		    }		    
		}
		Scalar color = Scalar( 0, 0, 255 );
		/* Todo:  If multiple, draw the largest contour */
		drawContours( lines, valid_target_contours, largest_contour_area_index, color, 2, 8, hierarchy, 0, Point() );
	}

    /// Detect Tower Lights and Shield Edge Lights
	/// Screen out all but interesting contours
	vector<interesting_contour> potential_lights; 	
	//Mat lines = Mat::zeros( thresholded_lights_image.size(), CV_8UC3 );
	for( int i = 0; i< contours.size(); i++ )
	{
		vector<Point> vp = contours[i];
		Rect boundRect = boundingRect(vp);		
		if(boundRect.height < 3 || boundRect.width < 3){
			continue;
		}
		/*
		float aspect = (float)boundRect.width/(float)boundRect.height;
		if(aspect < 1.0) {
			continue;
		}
		*/
		Scalar color = Scalar( 255, 255, 255 );
		drawContours( lines, contours, i, color, 1, 8, hierarchy, 0, Point() );
		
        interesting_contour ic;
	    Moments mu = moments(contours[i],false);
	    ic.pt.x = (int) (mu.m10 / mu.m00);
	    ic.pt.y = (int) (mu.m01 / mu.m00);
	    ic.bounding_rect = boundRect;		
	    cv::circle( lines, ic.pt, 3, Scalar(0,0,255), -1);
        potential_lights.push_back(ic);		
	}

    int max_vert_collinear_points = INT_MIN;
    int max_horz_collinear_points = INT_MIN;
    std::cout << "New Frame" << std::endl;
    vector<interesting_contour *> v_contours_by_angle[360];
    vector<interesting_contour *> h_contours_by_angle[360];
   
    for ( int i = 0; i < potential_lights.size(); i++ ) {
        bool i_at_v_angle[360]; 
        bool i_at_h_angle[360]; 
        memset(i_at_v_angle,0,sizeof(i_at_v_angle));
        memset(i_at_h_angle,0,sizeof(i_at_h_angle));
        for ( int j = 0; j < potential_lights.size(); j++ ) {
            if ( i != j ) {
                float angle = angle_between( potential_lights[i].pt, potential_lights[j].pt );
                if ( ( ( angle > (360.0 - VERTICAL_LIGHTS_ANGLE_RANGE) ) &&
                       ( angle < (0.0 + VERTICAL_LIGHTS_ANGLE_RANGE) ) ) ||
                     ( ( angle > (180.0 - VERTICAL_LIGHTS_ANGLE_RANGE) ) &&
                       ( angle < (180.0 + VERTICAL_LIGHTS_ANGLE_RANGE) ) ) ) {
                    /* Possibly a vertical collinear point */
                    /* Normalize angle so it runs from top of image to botton */
                    if ( potential_lights[i].pt.y > potential_lights[j].pt.y ) {
                        angle = angle_between( potential_lights[j].pt, potential_lights[i].pt);
                    }
                    if ( angle == 360 ) { 
                        angle = 0;
                    }
                    if ( !i_at_v_angle[(int)angle] ) {
                        i_at_v_angle[(int)angle] = true;
                        v_contours_by_angle[(int)angle].push_back(&potential_lights[i]);
                    }
                    v_contours_by_angle[(int)angle].push_back(&potential_lights[j]);
                }
                if ( ( ( angle > (90.0 - HORIZONTAL_LIGHTS_ANGLE_RANGE) ) &&
                       ( angle < (90.0 + HORIZONTAL_LIGHTS_ANGLE_RANGE) ) ) ||
                     ( ( angle > (270.0 - HORIZONTAL_LIGHTS_ANGLE_RANGE) ) &&
                       ( angle < (270.0 + HORIZONTAL_LIGHTS_ANGLE_RANGE) ) ) ) {
                    /* Possibly a horizontal collinear point */
                    /* Normalize angle so it runs from left of image to right */
                    if ( potential_lights[i].pt.x > potential_lights[j].pt.x ) {
                        angle = angle_between( potential_lights[j].pt, potential_lights[i].pt);
                    }
                    if ( angle == 360 ) { 
                        angle = 0;
                    }                    
                    if ( !i_at_h_angle[(int)angle] ) {
                        i_at_h_angle[(int)angle] = true;
                        h_contours_by_angle[(int)angle].push_back(&potential_lights[i]);
                    }
                    h_contours_by_angle[(int)angle].push_back(&potential_lights[j]);
                }                
            }
        }
    }

    /* Determine which Vertical angles represent the "dominant" angle */
    /* (the angle with the greatest number of point intersections).   */

    int vert_per_angle_max_contour_count = -1;
    int horz_per_angle_max_contour_count = -1;
    int dominant_vert_angle = -1;
    int dominant_horz_angle = -1;
    for ( int angle = 0; angle < 360; angle++ ) {
        int intersect_count = 0;
        for ( int a = angle - COLLINEAR_ANGLE_RANGE; a < angle + COLLINEAR_ANGLE_RANGE; a++ ) {
            int index = (a < 0) ? 360 + a : a;
            intersect_count += v_contours_by_angle[index].size();
        }
        if ( intersect_count > vert_per_angle_max_contour_count ) {
            vert_per_angle_max_contour_count = intersect_count;
            dominant_vert_angle = angle;
        }
    }
    
    /* Determine which Horizontal angles represent the "dominant" angle */

    for ( int angle = 0; angle < 360; angle++ ) {
        int intersect_count = 0;
        for ( int a = angle - COLLINEAR_ANGLE_RANGE; a < angle + COLLINEAR_ANGLE_RANGE; a++ ) {
            int index = (a < 0) ? 360 + a : a;
            intersect_count += h_contours_by_angle[index].size();
        }
        if ( intersect_count > horz_per_angle_max_contour_count ) {
            horz_per_angle_max_contour_count = intersect_count;
            dominant_horz_angle = angle;
        }
    }
    
    /* Multiple lines may exist which intersect points along the "dominant" angle   */
    /* Select the line having the greatest number of points which fall within the   */
    /* (possibly rotated) rectangle formed by the angle, each interesting point,    */
    /* and the COLLINEAR_ANGLE_RANGE                                                */ 
    
    /* If the "dominant" angles have sufficient points, draw the points along */
    /* the dominant vertical and horizontal angles.                           */

    int nth_triangle_collinear_pts = MIN_COLLINEAR_POINTS - 1;
    for ( int i = nth_triangle_collinear_pts - 1; i > 0; i-- ) {
       nth_triangle_collinear_pts += i;
    }

	std::cout << "NumPotentialLights:  " << potential_lights.size() << std::endl;
	
    bool detected_tower_lights = false;	    
    /** Vertical (Tower) Detection */	
	
    vector<interesting_contour *>v_contours_in_dom_angle_range;
    if ( vert_per_angle_max_contour_count > nth_triangle_collinear_pts ) {
        for ( int a = dominant_vert_angle - COLLINEAR_ANGLE_RANGE; a < dominant_vert_angle + COLLINEAR_ANGLE_RANGE; a++ ) {
            int index = (a < 0) ? 360 + a : a;    	
            for ( int i = 0; i < v_contours_by_angle[index].size(); i++ ) {
                bool found = false;
                for ( int x = 0; x < v_contours_in_dom_angle_range.size(); x++ ) {
                    if (((*v_contours_in_dom_angle_range[x]).pt.x ==
                         (*v_contours_by_angle[index][i]).pt.x) &&  
                        ((*v_contours_in_dom_angle_range[x]).pt.y ==
                         (*v_contours_by_angle[index][i]).pt.y)) {
                         found = true;
                         break;
                     }  
                }
                if ( !found ) {
                    v_contours_in_dom_angle_range.push_back( v_contours_by_angle[index][i] );
                }
	        }
	    }
	}
	    
    int max_vert_intersecting_contours = -1;
    enclosing_polygon max_vert_intersection_enc_poly;
    vector<interesting_contour *>max_vert_int_contours;
	    
    for ( int i = 0; i < v_contours_in_dom_angle_range.size(); i++ ) {
        enclosing_polygon enclosing_poly;
        project_pt_and_angle_to_square(
            (*(v_contours_in_dom_angle_range[i])).pt,
            dominant_vert_angle,
            COLLINEAR_ANGLE_RANGE,
            enclosing_poly);
        int intersecting_contour_count = 0;
        vector<interesting_contour *>intersecting_vert_contours;
        bool found_intersection = false;
        for ( int j = 0; j < v_contours_in_dom_angle_range.size(); j++ ) {
            if ( i != j ) {
                if ( point_in_polygon( (*(v_contours_in_dom_angle_range[j])).pt,
                                       enclosing_poly.box,
                                       sizeof(enclosing_poly.box)/
                                           sizeof(enclosing_poly.box[0]))) {
                    intersecting_contour_count++;
                    intersecting_vert_contours.push_back(v_contours_in_dom_angle_range[j]);
                    found_intersection = true;
                } 
            }                       
        }
        if ( found_intersection ) {
            intersecting_contour_count++;
            intersecting_vert_contours.push_back(v_contours_in_dom_angle_range[i]);
        }
        if ( intersecting_contour_count > max_vert_intersecting_contours ) {
            max_vert_intersecting_contours = intersecting_contour_count;
            max_vert_intersection_enc_poly = enclosing_poly;
            max_vert_int_contours = intersecting_vert_contours;
        }
    }
	
	if ( ( max_vert_intersecting_contours > 0 ) &&
	     ( max_vert_int_contours.size() >= MIN_COLLINEAR_POINTS ) ) {
	    detected_tower_lights = true;
	    std::cout << "NumVContours:  " << max_vert_int_contours.size() << std::endl;
	    for ( int i = 0; i < max_vert_int_contours.size(); i++ ) {
	        cv::circle( lines, (*(max_vert_int_contours[i])).pt, 5, Scalar(0,255,0), -1);	            	    
        }
        for ( int i = 0; i < 4; i++ ) {
            line( lines, max_vert_intersection_enc_poly.box[i], max_vert_intersection_enc_poly.box[(i+1)%4], Scalar(0,255,0));
        }
        line( lines, max_vert_intersection_enc_poly.centerline_begin, 
                     max_vert_intersection_enc_poly.centerline_end,
                     Scalar(255,255,0));
	}
	
	/* Horizontal (Shield Edge) Detection */
	    
    vector<interesting_contour *>h_contours_in_dom_angle_range;
    if ( horz_per_angle_max_contour_count > nth_triangle_collinear_pts ) {
        for ( int a = dominant_horz_angle - COLLINEAR_ANGLE_RANGE; a < dominant_horz_angle + COLLINEAR_ANGLE_RANGE; a++ ) {
            int index = (a < 0) ? 360 + a : a;    	
            for ( int i = 0; i < h_contours_by_angle[index].size(); i++ ) {
                bool found = false;
                for ( int x = 0; x < h_contours_in_dom_angle_range.size(); x++ ) {
                    if (((*h_contours_in_dom_angle_range[x]).pt.x ==
                         (*h_contours_by_angle[index][i]).pt.x) &&  
                        ((*h_contours_in_dom_angle_range[x]).pt.y ==
                         (*h_contours_by_angle[index][i]).pt.y)) {
                         found = true;
                         break;
                     }  
                }
                if ( !found ) {
                    h_contours_in_dom_angle_range.push_back( h_contours_by_angle[index][i] );
                }
	        }
	    }
	}

    int max_horz_intersecting_contours = -1;
    enclosing_polygon max_horz_intersection_enc_poly;
    vector<interesting_contour *>max_horz_int_contours;
	    
    for ( int i = 0; i < h_contours_in_dom_angle_range.size(); i++ ) {
        enclosing_polygon enclosing_poly;
        project_pt_and_angle_to_square(
            (*(h_contours_in_dom_angle_range[i])).pt,
            dominant_horz_angle,
            COLLINEAR_ANGLE_RANGE,
            enclosing_poly);
        int intersecting_contour_count = 0;
        vector<interesting_contour *>intersecting_horz_contours;
        bool found_intersection = false;
        for ( int j = 0; j < h_contours_in_dom_angle_range.size(); j++ ) {
            if ( i != j ) {
                if ( point_in_polygon( (*(h_contours_in_dom_angle_range[j])).pt,
                                       enclosing_poly.box,
                                       sizeof(enclosing_poly.box)/
                                           sizeof(enclosing_poly.box[0]))) {
                    intersecting_contour_count++;
                    intersecting_horz_contours.push_back(h_contours_in_dom_angle_range[j]);
                    found_intersection = true;
                } 
            }                       
        }
        if ( found_intersection ) {
            intersecting_contour_count++;
            intersecting_horz_contours.push_back(h_contours_in_dom_angle_range[i]);
        }
        if ( intersecting_contour_count > max_horz_intersecting_contours ) {
            max_horz_intersecting_contours = intersecting_contour_count;
            max_horz_intersection_enc_poly = enclosing_poly;
            max_horz_int_contours = intersecting_horz_contours;
        }
    }
	
	if ( (!detected_tower_lights) &&
	     ( max_horz_intersecting_contours > 0 ) &&
	     ( max_horz_int_contours.size() >= MIN_COLLINEAR_POINTS ) ) {
	    std::cout << "NumHContours:  " << max_horz_int_contours.size() << std::endl;
	    for ( int i = 0; i < max_horz_int_contours.size(); i++ ) {
	        cv::circle( lines, (*(max_horz_int_contours[i])).pt, 5, Scalar(255,0,0), -1);	            	    
        }
        for ( int i = 0; i < 4; i++ ) {
            line( lines, max_horz_intersection_enc_poly.box[i], max_horz_intersection_enc_poly.box[(i+1)%4], Scalar(255,0,0));
        }
        line( lines, max_horz_intersection_enc_poly.centerline_begin, 
                     max_horz_intersection_enc_poly.centerline_end,
                     Scalar(0,255,255));
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

void process_frame_shield_divider( Mat& frame, Mat& grayscale, Mat& grayscale_blur, Mat& cdst, Mat& edges, algorithm_results& results ) 
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

int process_circles(Mat& src, Mat& grayscale, Mat& cdst, algorithm_results& results)
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
