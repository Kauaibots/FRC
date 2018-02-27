#include <Arduino.h>
JoyState_t joySt;		// Joystick state structure
#include <LiquidCrystal.h>
float sensorValue1 = 0;
float sensorOutput1 = 0;
LiquidCrystal lcd(8, 9, 10, 11, 12, 13);

//DESIGNS CUSTOM CHARACTER TO DISTINGUISH INDIVIDUAL SLIDER VALUES
byte OVERLAY [8] = {
  B00010,       
  B00100,
  B01100,
  B11111,
  B00110,
  B00100,
  B01000,
  B10000,
};

//DECLARES BUTTON VARIABLES
  int datapin = 2;
  int clockpin = 1;
  int latchpin = 0;
  int button[17] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
  byte data = 0;
  byte switchVar1 = 72;
  byte switchVar2 = 159;

void setup() {
//CREATES CUSTOM CHARACTER "OVERLAY" THEN CLEARS AND INITIALIZES 16 X 2 LCD DISPLAY
  lcd.createChar(0, OVERLAY);
  lcd.clear();
  lcd.begin(16, 2);
//INITIALIZES TO SERIAL MONITOR
  Serial.begin(9600);
  pinMode(datapin, INPUT);
  pinMode(clockpin, OUTPUT);
  pinMode(latchpin, OUTPUT);
 
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
//READS SLIDER VALUE
  float sensorValue1 = analogRead(A0);
//CONVERTS VALUE TO BE SHOWN ON LCD TO A RANGE OF 0.0-2.0
  float sensorOutput1 = -1.9 + (sensorValue1 * 0.00391);
  if(sensorOutput1 < 0)
  {
   sensorOutput1 = 0;
  }
  if(sensorOutput1 > 2)
  {
   sensorOutput1 = 2;
  }
  joySt.xAxis = (uint8_t)(sensorOutput1*127.5);
  float sensorValue2 = analogRead(A1);
//CONVERTS VALUE TO BE SHOWN ON LCD TO A RANGE OF 0.0-2.0
  float sensorOutput2 = -1.9 + (sensorValue2 * 0.00391);
  if(sensorOutput2 < 0)
  {
   sensorOutput2 = 0;
  }
  if(sensorOutput2 > 2)
  {
   sensorOutput2 = 2;
  }
  joySt.yAxis = (uint8_t)(sensorOutput2*127.5);
    float sensorValue3 = analogRead(A2);
//CONVERTS VALUE TO BE SHOWN ON LCD TO A RANGE OF 0.0-2.0
  float sensorOutput3 = -1.9 + (sensorValue3 * 0.00391);
  if(sensorOutput3 < 0)
  {
   sensorOutput3 = 0;
  }
  if(sensorOutput3 > 2)
  {
   sensorOutput3 = 2;
  }
    joySt.throttle = (uint8_t)(sensorOutput3*127.5);
    float sensorValue4 = analogRead(A3);
//CONVERTS VALUE TO BE SHOWN ON LCD TO A RANGE OF 0.0-2.0
  float sensorOutput4 = -1.9 + (sensorValue4 * 0.00391);
  if(sensorOutput4 < 0)
  {
   sensorOutput4 = 0;
  }
  if(sensorOutput4 > 2)
  {
   sensorOutput4 = 2;
  }
 joySt.rudder = (uint8_t)(sensorOutput4*127.5);
//PRINTS SENSOR VALUE TO SERIAL MONITOR
//*NOTE* LCD SHOWS sensorOutput NOT ACTUAL VALUE OF SLIDER
  //Serial.println(sensorValue1);
  //delay(100); 

//SHOWS EACH VALUE CORRESPONDING TO EACH SLIDER
  lcd.setCursor(0,2);   //SLIDER 1
  //lcd.write(byte(0));
  lcd.print(sensorOutput1);

  lcd.setCursor(4,2);   //SLIDER 2
  //lcd.write(byte(0));
  lcd.print(sensorOutput2);
 
  lcd.setCursor(8,2);  //SLIDER 3
  //lcd.write(byte(0));
  lcd.print(sensorOutput3);
 
  lcd.setCursor(12,2);  //SLIDER 4
  //lcd.write(byte(0));
  lcd.print(sensorOutput4);
  
  
//COVERS THIRD DIGIT OF SLIDER VALUE//
  lcd.setCursor(3,2);  //SLIDER 1
  //lcd.print(" ");
  lcd.write(byte(0));  
  
  lcd.setCursor(7,2);  //SLIDER 2
  //lcd.print(" ");
  lcd.write(byte(0));
  
  lcd.setCursor(11,2); //SLIDER 3
  //lcd.print(" ");
  lcd.write(byte(0));
  
  lcd.setCursor(15,2); //SLIDER 4
  //lcd.print(" ");
  lcd.write(byte(0));
    
//BUTTON CODE DONE BY MATT
  digitalWrite(latchpin, 1);
  delay(10);
  digitalWrite(latchpin, 0);
  
  switchVar1 = shiftIn(datapin, clockpin);
  switchVar2 = shiftIn(datapin, clockpin);
  
  /*Serial.print(switchVar1, HEX);
  Serial.print(switchVar2, HEX);
  Serial.print("" "");*/
  int i;
  for (int n=0; n<=7; n++)
  {
    //so, when n is 3, it compares the bits
    //in switchVar1 and the binary number 00001000
    //which will only return true if there is a 
    //1 in that bit (ie that pin) from the shift
    //register.
    if (switchVar1 & (1 << 7-n) ){
     button[n] = HIGH;
     }else{
       button[n] = LOW;
    }
    
    if (switchVar2 & (1 << 7-n) ){
     button[n+8] = HIGH;
     }else{
       button[n+8] = LOW;
    }
  }
  
    joySt.buttons = 0;
    for ( int x = 0; x < 16; x++ ) {
      if ( button[x] == HIGH ) {
        joySt.buttons |= (1 << x); 
      }
    }


  
  for (i=0; i<17; i++)
  {
    Serial.print(button[i], BIN);
  }
  Serial.println();
  delay(10);
  //READS BUTTON STATES AND DISPLAYS IT ON FIRST ROW OF LCD
//KEY: 0 means "unpressed"
//     1 means "presssed"
  lcd.setCursor(0,0);  //BUTTON 0 7
  lcd.print(button[0]);
  
  lcd.setCursor(1,0);  //BUTTON 1
  lcd.print(button[1]);
  
  lcd.setCursor(2,0);  //BUTTON 2
  lcd.print(button[2]);
  
  lcd.setCursor(3,0);  //BUTTON 3
  lcd.print(button[3]);
  
  lcd.setCursor(4,0);  //BUTTON 4
  lcd.print(button[4]);
  
  lcd.setCursor(5,0);  //BUTTON 5
  lcd.print(button[5]);
  
  lcd.setCursor(6,0);  //BUTTON 6
  lcd.print(button[6]);
  
  lcd.setCursor(7,0);  //BUTTON 7
  lcd.print(button[7]);
  
  lcd.setCursor(8,0);  //BUTTON 8
  lcd.print(button[8]);
  
  lcd.setCursor(9,0);  //BUTTON 9
  lcd.print(button[9]);
  
  lcd.setCursor(10,0);  //BUTTON 10
  lcd.print(button[10]);
  
  lcd.setCursor(11,0);  //BUTTON 11
  lcd.print(button[11]);
  
  lcd.setCursor(12,0);  //BUTTON 12
  lcd.print(button[12]);
  
  lcd.setCursor(13,0);  //BUTTON 13
  lcd.print(button[13]);
  
  lcd.setCursor(14,0);  //BUTTON 14
  lcd.print(button[14]);
  
  lcd.setCursor(15,0);  //BUTTON 15
  lcd.print(button[15]);
  Joystick.setState(&joySt);

}


byte shiftIn(int myDataPin, int myClockPin) { 
  int i;
  int temp = 0;
  int pinState;
  byte myDataIn = 0;


  pinMode(myClockPin, OUTPUT);
  pinMode(myDataPin, INPUT);
//we will be holding the clock pin high 8 times (0,..,7) at the
//end of each time through the for loop


//at the begining of each loop when we set the clock low, it will
//be doing the necessary low to high drop to cause the shift
//register's DataPin to change state based on the value
//of the next bit in its serial information flow.
//The register transmits the information about the pins from pin 7 to pin 0
//so that is why our function counts down
  for (i=7; i>=0; i--)
  {
    digitalWrite(myClockPin, 0);
    delayMicroseconds(2);
    temp = digitalRead(myDataPin);
    if (temp) {
      pinState = 1;
      //set the bit to 0 no matter what
      myDataIn = myDataIn | (1 << i);
    }
    else {
      //turn it off -- only necessary for debuging
     //print statement since myDataIn starts as 0
      pinState = 0;
    }


    //Debuging print statements
    //Serial.print(pinState);
    //Serial.print("     ");
    //Serial.println (dataIn, BIN);


    digitalWrite(myClockPin, 1);


  }
  //debuging print statements whitespace
  //Serial.println();
  //Serial.println(myDataIn, BIN);
  return myDataIn;
}

