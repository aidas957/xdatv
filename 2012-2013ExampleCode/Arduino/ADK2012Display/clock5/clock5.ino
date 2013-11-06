#include <ADK.h>

ADK adk;
String message="xda-developers is cool";


int colors [3] [3] ={
    {0,255,0},
    {255,0,0},
    {0,0,255}
};

int colorcolor[][];
void setup(void){
    adk.adkInit();
    message="      "+message;
}


int stringTrim=0;

void loop(){
  stringTrim++;
  String newString="";

  if (stringTrim>message.length()){
       stringTrim=0;
  }
   for (int i=0; i<6; i++) {
      if ( stringTrim+i>=message.length()){
        newString=newString+" ";
      } else {
        newString=newString+message.charAt(stringTrim+i);
      }
   }  
   
  

   for(int i=0;i<6;i++){
        int x=random(0,3);
        adk.ledDrawLetter(i,newString.charAt(i),colors[x][0],colors[x][1],colors[x][2]);
    }
        delay(300);

}

