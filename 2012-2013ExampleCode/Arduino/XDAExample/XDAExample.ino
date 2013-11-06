 /*XDA Example 
 Copyright (C) 2012 XDA-Developers
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


//Define our variables
 int LEDPin[]={ 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52 } ;
 int LEDCount=16;    //number of LEDs

void setup() {
   for (int Pin=0; Pin < LEDCount; Pin++)  {  //initialize each LED 
     pinMode(LEDPin[Pin], OUTPUT);          
     digitalWrite(LEDPin[Pin], HIGH);         //Turn the LED on
     delay(50);
     digitalWrite(LEDPin[Pin], LOW);          //Turn the LED back off
   }
  
  
}

void loop() {
   for (int Pin=0; Pin < LEDCount; Pin++)  {  
     digitalWrite(LEDPin[Pin], HIGH);         //Turn the LED on
     delay(50);
   }
   for (int Pin=0; Pin < LEDCount; Pin++)  {  
     delay(50);
     digitalWrite(LEDPin[Pin], LOW);          //Turn the LED back off
   }
  
}
