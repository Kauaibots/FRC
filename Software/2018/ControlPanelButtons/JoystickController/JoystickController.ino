JoyState_t joySt;

// arduino pins for each button
const int button1 = 2;   // elevator "hover" position while traveling with cube
const int button2 = 3;   // elevator switch position
const int button3 = 4;   // elevator low scale
const int button4 = 5;   // elevator medium scale
const int button5 = 6;   // elevator high scale (no button on board, use joystick buttons for now)
const int button6 = 7;   // open close claw (default open? default closed? not sure) No button on board yet, need to use joystick buttons for now
const int button7 = 8;   // claw in
const int button8 = 9;   // claw out
const int button9 = 10;  // 90 degrees
const int button10 = 11; // 0 degrees
const int button11 = 12; // -90 degrees
const int button12 = 13; // 180 degrees
//const int button13 = A0; // climb, not yet on the board, just here for if/when we ever need it

/* mask for changing bits (each button is 1 "digit" greater than the last)
   e.g. mask for button 1 = 1; button 2 = 10; button 3 = 100 and so on
   
   button binary data is only compared to the masks if the pin is LOW, meaning
   the button is pressed, so the "1" in the mask will be added to the button binary
   nummber, representing that that button is pressed
*/
const int mask1 = 1;
const int mask2 = 2;
const int mask3 = 4;
const int mask4 = 8;
const int mask5 = 16;
const int mask6 = 32;
const int mask7 = 64;
const int mask8 = 128;
const int mask9 = 256;
const int mask10 = 512;
const int mask11 = 1024;
const int mask12 = 2048;
const int mask13 = 4096;

void setup() {
  Serial.begin(9600);
  /*  INPUT_PULLUP mode is used to prevent input pins from floating
      between HIGH and LOW. Also removes the need for resistors, and buttons
      are connected to ground not 5v
  */
  pinMode(button1, INPUT_PULLUP);
  pinMode(button2, INPUT_PULLUP);
  pinMode(button3, INPUT_PULLUP);
  pinMode(button4, INPUT_PULLUP);
  pinMode(button5, INPUT_PULLUP);
  pinMode(button6, INPUT_PULLUP);
  pinMode(button7, INPUT_PULLUP);
  pinMode(button8, INPUT_PULLUP);
  pinMode(button9, INPUT_PULLUP);
  pinMode(button10, INPUT_PULLUP);
  pinMode(button11, INPUT_PULLUP);
  pinMode(button12, INPUT_PULLUP);
  pinMode(button13, INPUT);
  
  // set all joystick values to 0
  joySt.xAxis = 0;
  joySt.yAxis = 0;
  joySt.zAxis = 0;
  joySt.xRotAxis = 0;
  joySt.yRotAxis = 0;
  joySt.zRotAxis = 0;
  joySt.throttle = 0;
  joySt.rudder = 0;
  joySt.hatSw1 = 0;
  joySt.hatSw2 = 0;
  joySt.buttons = 0;

}

void loop() {
  Serial.println(analogRead(button13));
  // the running binary data of button presses
  int buttonData = 0;
  
  /*
    If the pin is low, meaning button is being pressed, it will add a 1 in
    that button's place in the binary buttonData by comparing the current buttonData
    with the given button's mask, and then changing buttonData to be the bitwise OR of
    the two values.
    
    OR compares the bits of each binary number. If there is a '1' in either or both of
    the bits, the new bit is a '1', else it is '0' if both bits being compared are 0.
  */
  
  if (digitalRead(button1) == LOW) {
    buttonData = buttonData | mask1;
  }
  if (digitalRead(button2) == LOW) {
    buttonData = buttonData | mask2;
  }
  if (digitalRead(button3) == LOW) {
    buttonData = buttonData | mask3;
  }
  if (digitalRead(button4) == LOW) {
    buttonData = buttonData | mask4;
  }
  if (digitalRead(button5) == LOW) {
    buttonData = buttonData | mask5;
  }
  if (digitalRead(button6) == LOW) {
    buttonData = buttonData | mask6;
  }
  if (digitalRead(button7) == LOW) {
    buttonData = buttonData | mask7;
  }
  if (digitalRead(button8) == LOW) {
    buttonData = buttonData | mask8;
  }
  if (digitalRead(button9) == LOW) {
    buttonData = buttonData | mask9;
  }
  if (digitalRead(button10) == LOW) {
    buttonData = buttonData | mask10;
  }
  if (digitalRead(button11) == LOW) {
    buttonData = buttonData | mask11;
  }
  if (digitalRead(button12) == LOW) {
    buttonData = buttonData | mask12;
  }
  if (digitalRead(button13) == HIGH) {
    buttonData = buttonData | mask13;
  }

  // set joystick buttons value to button data and send it
  joySt.buttons = buttonData;
  Joystick.setState(&joySt);
}
