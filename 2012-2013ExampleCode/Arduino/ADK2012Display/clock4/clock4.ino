#include <ADK.h>

ADK adk;
String message="xdadev";


int colors [3] [3] ={
    {0,255,0},
    {255,0,0},
    {0,0,255}
};

void setup(void){
    adk.adkInit();
}


void loop(){
  for (int x=0;x<3;x++){
    for(int i=0;i<6;i++){
        //x=random(0,3);
        adk.ledDrawLetter(i,message.charAt(i),colors[x][0],colors[x][1],colors[x][2]);
        delay(10);
    }
  }

}

