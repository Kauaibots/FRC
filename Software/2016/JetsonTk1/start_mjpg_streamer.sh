mmc_path=/mnt/sdcard/streamer/
echo $mmc_path
rm -r $mmc_path*
mkdir $mmc_path
export LD_LIBRARY_PATH=/usr/local/lib 
/usr/local/bin/mjpg_streamer -i "input_file.so -r -d 66 -f $mmc_path -n current.jpg" -o "output_http.so -w /usr/local/www -p 5800" &
   
