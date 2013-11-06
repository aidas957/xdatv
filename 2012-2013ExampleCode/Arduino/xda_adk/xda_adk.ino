#include <Wire.h>
#include <Usb.h>
#include <AndroidAccessory.h>

//Setup Accessory
AndroidAccessory acc("XDA-Developers",
             "XDA-Demo",
             "XDA-Demo Arduino Board",
             "1.0",
             "http://www.xda-developers.com",
             "0000000012345678");

void setup();
void loop();


//define our pin array
int LED[]={22,24,26,28,30,32,34,36,38,40,42,44,46,48,50,52};
int minPin=22;
int maxPin=52;

void init_leds()
{
  for (int thisPin=minPin; thisPin <= maxPin; thisPin++) { 
       pinMode(thisPin, OUTPUT);
       // turn the pin on:
       digitalWrite(thisPin, HIGH);   
       delay(50);
       digitalWrite(thisPin, LOW);    
       Serial.print("initializing pin");
       Serial.println(thisPin);
       thisPin++;       
  }
}
void off_leds()
{
  for (int thisPin=minPin; thisPin <= maxPin; thisPin++) { 
       delay(50);
       digitalWrite(thisPin, LOW);    
       Serial.print("Turning off ");
       Serial.println(thisPin);
       thisPin++;       
  }
}
void setup()

{
    Serial.begin(115200);
    Serial.print("\r\nStart");
    init_leds();
    acc.powerOn();
}
void loop() {
    byte msg[3];
    if (acc.isConnected()) {
        int len = acc.read(msg, sizeof(msg), 1);
        if (len > 0) {
            if (msg[0] == 0x3) {
                Serial.print("Commanded LED ");
                Serial.print(msg[1]);
                Serial.print(" to value ");
                Serial.println(msg[2] );
                digitalWrite(LED[msg[1]], msg[2] ? HIGH : LOW);
            }
        }
    } else {
       // init_leds();
    }
    delay(10);
}

