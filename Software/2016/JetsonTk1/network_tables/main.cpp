#include <iostream>
#include <chrono>
#include <cstdio>
#include <thread>
#include "ntcore.h"

#include "networktables/NetworkTable.h"

using namespace std;

int main() {
  auto nt = NetworkTable::GetTable("videoproc");
  
  nt->SetClientMode();
  nt->SetIPAddress("192.168.0.112\n");
  
  nt->Initialize();
  std::this_thread::sleep_for(std::chrono::seconds(5));

  while (true) { 
    int i = rand() % 1000 + 1;
    nt->PutNumber("target_distance_inches", i);
    nt->PutNumber("target_angle_degrees", i+1);
    nt->PutBoolean("target_detected", (i%2) ? true : false);
    if ( nt->ContainsKey("enable_algorithm") ) {
       std::cout << "enable_algorithm:  " << nt->GetBoolean("enable_algorithm",false) << std::endl;
    }    
    if ( nt->ContainsKey("enable_stream_out") ) {
       std::cout << "enable_stream_out:  " << nt->GetBoolean("enable_stream_out",false) << std::endl;
    }  
    if ( nt->ContainsKey("enable_file_out") ) {
       std::cout << "enable_file_out:  " << nt->GetBoolean("enable_file_out",false) << std::endl;
    }  
    if ( nt->ContainsKey("algorithm") ) {
       std::cout << "algorithm:  " << nt->GetNumber("algorithm",-1.0) << std::endl;
    }
    if ( nt->ContainsKey("input_camera") ) {
       std::cout << "input_camera:  " << nt->GetNumber("input_camera",-1.0) << std::endl;
    } 
    if ( nt->ContainsKey("ping") ) {
       double ping_value = nt->GetNumber("ping",-1.0);
       std::cout << "ping:  " << ping_value << std::endl;
       nt->PutNumber("ping_response",ping_value);
    }        
    std::this_thread::sleep_for(std::chrono::milliseconds(200));
  }

}
