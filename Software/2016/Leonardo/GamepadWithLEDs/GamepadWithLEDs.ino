/*
  Copyright (c) 2014-2015 NicoHood
  See the readme for credit to other people.

  Gamepad example
  Press a button and demonstrate Gamepad actions

  You can also use Gamepad1,2,3 and 4 as single report.
  This will use 1 endpoint for each gamepad.

  See HID Project documentation for more infos
  https://github.com/NicoHood/HID/wiki/Gamepad-API
*/

#include <HID-Project.h>
#include "APA102.h"

// Define which pins to use.
const uint8_t dataPin = 11;
const uint8_t clockPin = 12;

// Create an object for writing to the LED strip.
APA102<dataPin, clockPin> ledStrip;

// Set the number of LEDs to control.
const uint16_t ledCount = 120;

// Create a buffer for holding the colors (3 bytes per color).
rgb_color colors[ledCount];

const int pinLed = LED_BUILTIN;
const int pinButton = 2;

void setup() {
  pinMode(pinLed, OUTPUT);
  pinMode(pinButton, INPUT_PULLUP);
  Serial.begin(57600);
  // Sends a clean report to the host. This is important on any Arduino type.
  GamepadWithLEDs.begin();
}

//red, yellow, purple, blue, orange, green
int led_start_index_per_section[] = { 0, 22, 40, 58, 80, 98 }; 
int num_leds_per_section[] = { 22, 18, 18, 22, 18, 22 };
int red_value_per_section[] = {255, 255, 255, 0, 255, 0 };
int green_value_per_section[] = { 0, 255, 0, 0, 165, 255 };
int blue_value_per_section[] = { 0, 0, 255, 255, 0, 0 };
int intensity_per_section[] = { 10, 10, 10, 10, 10, 10 };

void setLight(int color, boolean lightState)
{  
    if ( 0 <= color <=5 )
    {
     if (lightState == true)
        {
          intensity_per_section[color] = 10;
        }
     else
        {
          intensity_per_section[color] = 0;
        }
    }
}

void loop()
{
  static uint8_t button_loop = 0;
  static uint8_t curr_button = 0;
  if ( ( button_loop++ % 10 ) == 0 ) {
    GamepadWithLEDs.releaseAll();
    GamepadWithLEDs.press(curr_button);
    GamepadWithLEDs.write();
    curr_button = curr_button + 1;
    if ( curr_button > 32 ) {
      curr_button = 0;
    }    
  }

  uint32_t leds = GamepadWithLEDs.getLeds();
  
  //Serial.print("Len:  ");
  //Serial.print(GamepadWithLEDs.getLastLedReportLen());
  Serial.print("Leds:  ");
  Serial.println(leds,HEX);

  /* Change LED sections based upon lowest 6 bits of the leds variable */

  for ( uint32_t i = 0; i < 6; i++ ) {
    if ( ( leds & (1 << i) ) != 0 ) {
      setLight( (int)i, true ); 
    } else {
      setLight( (int)i, false );
    }
  }

  /* Update LEDs */
      
  ledStrip.startFrame();
  for(uint16_t i = 0; i < ledCount; i++)
  {
    for (int x = 0; x < 6; x++ ) {
      if ( ( led_start_index_per_section[x] <= i ) &&
           ( ( x == 5 ) || 
             ( i < led_start_index_per_section[x+1] ) ) ) {
         ledStrip.sendColor(
             red_value_per_section[x],
             green_value_per_section[x],
             blue_value_per_section[x],
             intensity_per_section[x]);
             break;     
      }
    }
  }
  ledStrip.endFrame(ledCount);  

  delay(5);  
}
