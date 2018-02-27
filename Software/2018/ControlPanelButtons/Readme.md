Arduino Control Panel Code - works with custom shield and reads 17 buttons.

Instructions:

- Check out this repository to your computer
- remove old arduino installed software versions
- Install an older arduino IDE version (1.6.3) - choose Windows Installer option.
  URL  https://www.arduino.cc/en/Main/OldSoftwareReleases#previous 
- Open Windows Explorer on the location where Arduino 1.6.3 was installed
  Should be:  C:\Program Files (x86)\Arduino
- Navigate to this subdirectory:
  C:\Program Files (x86)\Arduino\hardware\arduino\avr\cores\arduino
- Rename the existing HID.cpp and USBAPI.h files (e.g., tack on "old" to the name)
- Replace the existing files w/the HID.cpp and USBAPI.h file in this directory
- Open Arduino IDE
- File->Preferences
  - Update Sketchbook location to be parent of the "ControlPanelButtons" directory (this directory) 
- Open Sketch via File->Sketchbook->ControlPanelButtons
- Select Tools->Board->Arduino Leonardo
- Close Arduino IDE, re-open it