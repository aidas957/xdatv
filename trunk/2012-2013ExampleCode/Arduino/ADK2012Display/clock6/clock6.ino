#include <ADK.h>

ADK adk;
String message="xda-developers is cool";
int x=message.length();

int colors [3] [3] ={
    {0,255,0},
    {255,0,0},
    {0,0,255}
};

int maxMessageLength=100;
int colorshift[100][3];

void setup(void){
  //randomSeed(analogRead(0));
  colorshifter();
  adk.adkInit();
  message="      "+message;
}


int stringTrim=0;

void colorshifter(){
  for (int i=0; i<maxMessageLength; i++){
    int c=random(0,3);
    colorshift[i][0]=colors[c][0];
    colorshift[i][1]=colors[c][1];
    colorshift[i][2]=colors[c][2];
  }
}

void loop(){
  stringTrim++;
  String newString="";

  if (stringTrim>message.length()){
       stringTrim=0;
       colorshifter();
  }
   for (int i=0; i<6; i++) {
     if (stringTrim+i>=message.length()){
       newString=newString+" ";
     } else {
       newString=newString+message.charAt(stringTrim+i);
     }
   }  
  

   for(int i=0;i<6;i++){
      adk.ledDrawLetter(i,newString.charAt(i),colorshift[stringTrim+i][0],colorshift[stringTrim+i][1],colorshift[stringTrim+i][2]);
    }
      delay(300);
}


