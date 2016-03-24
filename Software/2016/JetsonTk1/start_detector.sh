#!/bin/bash
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/home/ubuntu/Kauaibots_FRC/Software/2016/JetsonTk1/network_tables/Linux/arm

#while true
#do
#if [ ! $(pgrep detector) ] ; then
#    echo "Starting Detector" >> /tmp/detector.log
#    /home/ubuntu/Kauaibots_FRC/Software/2016/JetsonTk1/detector roboRIO-2465-FRC.local > /tmp/detector.log
#fi
#sleep 1
#done    
/home/ubuntu/Kauaibots_FRC/Software/2016/JetsonTk1/detector roboRIO-2465-FRC.local
