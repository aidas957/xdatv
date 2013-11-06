#include <ADK.h>

ADK adk;
String message="dev";


void setup(void){
    adk.adkInit();
}


void loop(){
    for(int i=0;i<6;i++){
        adk.ledDrawLetter(i,message.charAt(i),0,255,0);
    }
}

