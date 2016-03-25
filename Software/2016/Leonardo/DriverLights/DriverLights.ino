/* This example shows how to display a moving rainbow pattern on
 * an APA102-based LED strip. */

/* By default, the APA102 uses pinMode and digitalWrite to write
 * to the LEDs, which works on all Arduino-compatible boards but
 * might be slow.  If you have a board supported by the FastGPIO
 * library and want faster LED updates, then install the
 * FastGPIO library and uncomment the next two lines: */
// #include <FastGPIO.h>
// #define APA102_USE_FAST_GPIO

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

// Set the brightness to use (the maximum is 31).
//const uint8_t brightness = 10;
void setup()
{
}
// Converts a color from HSV to RGB.
// h is hue, as a number between 0 and 360.
// s is the saturation, as a number between 0 and 255.
// v is the value, as a number between 0 and 255.
rgb_color hsvToRgb(uint16_t h, uint8_t s, uint8_t v)
{
    uint8_t f = (h % 60) * 255 / 60;
    uint8_t p = (255 - s) * (uint16_t)v / 255;
    uint8_t q = (255 - f * (uint16_t)s / 255) * (uint16_t)v / 255;
    uint8_t t = (255 - (255 - f) * (uint16_t)s / 255) * (uint16_t)v / 255;
    uint8_t r = 0, g = 0, b = 0;
    switch((h / 60) % 6){
        case 0: r = v; g = t; b = p; break;
        case 1: r = q; g = v; b = p; break;
        case 2: r = p; g = v; b = t; break;
        case 3: r = p; g = q; b = v; break;
        case 4: r = t; g = p; b = v; break;
        case 5: r = v; g = p; b = q; break;
    }
    
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
  static uint8_t curr_button = 0;
  GamepadWithLEDs.releaseAll();
  GamepadWithLEDs.press(curr_button);
  GamepadWithLEDs.write();
  curr_button = curr_button + 1;
  if ( curr_button > 32 ) {
    curr_button = 0;
  }    

  uint32_t leds = GamepadWithLEDs.getLeds();
  
  Serial.print("Len:  ");
  Serial.print(GamepadWithLEDs.getLastLedReportLen());
  Serial.print("Leds:  ");
  Serial.println(leds,HEX);

  /* Change LED sections based upon lowest 6 bits of the leds variable */

  for ( uint32_t i = 0; i < 6; i++ ) {
    if ( leds & (1 << i) ) != 0 ) {
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

  delay(100);  
}
