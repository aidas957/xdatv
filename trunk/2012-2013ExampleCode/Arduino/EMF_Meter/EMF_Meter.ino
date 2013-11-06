
 /*
  Hertz based electromagnetic field meter for Arduino.   
  Copyright (C) 2012 Adam Outler
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


 //A note on HZ, if you notice fluctuations on a known steady signal
 //  increase or decrease hertz by 1.
 float FrequencyToMonitor=59; //hertz
 

 //setup pins
 #define ANTENNA 31 //antenna pin
 //16 LEDs on 16 different pins
 int LEDPin[]={
 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52 } ;
 int LEDCount=16;    //number of LEDs

 //setup initial global values
 int LEDCounter=0;//initial value of LEDs to display
 int HalfPeriodMicroseconds=0;// initial value of halfwavelength

 /*
   SETUP defines pins and performs POT(Power On Test) on LEDs
 */
 void setup()  { 
   double HalfPeriod= ((1 / FrequencyToMonitor) * .5); //period from frequency
   HalfPeriodMicroseconds=int(1000000 * HalfPeriod);   //Convert period to microseconds

   pinMode(ANTENNA, INPUT);                   //set antenna pin
   for (int Pin=0; Pin < LEDCount; Pin++)  {  //initialize each LED 
     pinMode(LEDPin[Pin], OUTPUT);          
     digitalWrite(LEDPin[Pin], HIGH);         //Turn the LED on
     delay(50);
     digitalWrite(LEDPin[Pin], LOW);          //Turn the LED back off
   }
   swingLEDs();       //perform LED check to verify LED functionality
 } 


 /*
   LOOP checks for value of antenna to go high and then low
   if change is seen, then 2 LEDs are lit, otherwise total number
   of LEDS are decreased.  This allows for a magentic effect when
   near the field.
 */
 void loop(){
   int ANTENNAValue=0; //sets and resets ANTENNAValue
   int OldANTENNAValue=0; // as well as OldANTENNAValue
 

   for (int Pass=0; Pass < 4; Pass++)  {  //take 4 readings
     ANTENNAValue=digitalRead(ANTENNA);   //   from the antenna
     if ( ( ANTENNAValue == HIGH ) && ( OldANTENNAValue == LOW ) ) {
       incrementLED(); //If a change is detected increase twice
       incrementLED(); //   4 passes means 3 changes total
     } else if ( ( ANTENNAValue == LOW ) && ( OldANTENNAValue == HIGH ) ) {
       incrementLED(); //   This makes the meter rise by
       incrementLED(); //   a total factor of 1.5 and
     } else {
       decrementLED(); //   if there is no change it falls by 1
     }
       OldANTENNAValue=ANTENNAValue; //Log old value for 
       delayMicroseconds(HalfPeriodMicroseconds);  // half-wave pause before next reading
   }
   delayMicroseconds(HalfPeriodMicroseconds); //visual delay between reading passes
   delayMicroseconds(HalfPeriodMicroseconds); //twice to avoid extremely long numbers   
 }
 
 
 /*
 FUNCTION swingLEDs tests the LEDs by lighting
 them up sequentially.  Each LED is brough high
 then low twice. 
 */
 void swingLEDs(){
   for (int Pin=0; Pin < LEDCount; Pin++)  {
     digitalWrite(LEDPin[Pin], HIGH);  // instantanious full brightness
   }
   for (int Pin=0; Pin < LEDCount; Pin++)  {
     digitalWrite(LEDPin[Pin], LOW);   // clear LEDs from left to right
     delay(20);
   }
   for (int Pin=LEDCount; Pin > -1; Pin--)  {
     digitalWrite(LEDPin[Pin], HIGH);  // light LEDs from right to left
     delay(20);
   }
   for (int Pin=LEDCount; Pin > -1; Pin--)  {
     digitalWrite(LEDPin[Pin], LOW);   // clear LEDs from right to left
     delay(20);
   }
 }
 
 /* 
 FUNCTION incrementLED increases the number
 of LEDs lit by 1.  
 */
 void incrementLED(){
    if ( LEDCounter < 15 ){ ++LEDCounter;} //if not max increase
    digitalWrite(LEDPin[LEDCounter], HIGH); //light new LED
 }
 
 /*
 FUNCTION decrementLED increases the number
 of LEDs lit by 1.
 */
 void decrementLED(){
   digitalWrite(LEDPin[LEDCounter], LOW); //turn off current LED
   if (LEDCounter >= 0){ --LEDCounter;} // if not zero, increase
 }
