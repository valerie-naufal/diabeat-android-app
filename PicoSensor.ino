/*
  Read the 18 channels of spectral light over I2C using the Spectral Triad
  By: Nathan Seidle
  SparkFun Electronics
  Date: October 25th, 2018
  License: MIT. See license file for more information but you can
  basically do whatever you want with this code.

  This example takes all 18 readings and blinks the illumination LEDs 
  as it goes. We recommend you point the Triad away from your eyes, the LEDs are bright.
  
  Feel like supporting open source hardware?
  Buy a board from SparkFun! https://www.sparkfun.com/products/15050

  Hardware Connections:
  Plug a Qwiic cable into the Spectral Triad and a BlackBoard
  If you don't have a platform with a Qwiic connection use the SparkFun Qwiic Breadboard Jumper (https://www.sparkfun.com/products/14425)
  Open the serial monitor at 115200 baud to see the output
*/

#include "SparkFun_AS7265X.h" //Click here to get the library: http://librarymanager/All#SparkFun_AS7265X
AS7265X sensor;

#include <Wire.h>
#include <SerialBT.h>

void setup()
{
  Serial.begin(115200);
  // while(!Serial){
  //   ;
  // }
//  Serial.println("Point the Triad away and press a key to begin with illumination...");
//   while (Serial.available() == false)
//   {
//   }              //Do nothing while we wait for user to press a key
//   Serial.read(); //Throw away the user's button

  if (sensor.begin() == false)
  {
    Serial.println("Sensor does not appear to be connected. Please check wiring. Freezing...");
    while (1)
      ;
  }

  sensor.disableIndicator(); //Turn off the blue status LED
Serial.println("D,I,W,L");
  SerialBT.setName("PicoW Sensor");
  SerialBT.begin();

}

void loop()
{
  sensor.enableBulb(AS7265x_LED_IR);
  sensor.takeMeasurements();
 // sensor.takeMeasurementsWithBulb(); //This is a hard wait while all 18 channels are measured
  
  sensor.getCalibratedA(); //410nm
  sensor.getCalibratedB(); //435nm
  sensor.getCalibratedC(); //460nm
  // sensor.getCalibratedD(); //485nm
  float calibratedD = sensor.getCalibratedD();
  sensor.getCalibratedE(); //510nm
  sensor.getCalibratedF(); //535nm
  sensor.getCalibratedG(); //560nm
  sensor.getCalibratedH(); //585nm
  sensor.getCalibratedR(); //610nm
  // sensor.getCalibratedI(); //645nm
  float calibratedI = sensor.getCalibratedI();
  sensor.getCalibratedS(); //680nm
  sensor.getCalibratedJ(); //705nm
  sensor.getCalibratedT(); //730nm
  sensor.getCalibratedU(); //760nm
  sensor.getCalibratedV(); //810nm
  // sensor.getCalibratedW(); //860nm
  float calibratedW = sensor.getCalibratedW();
  sensor.getCalibratedK(); //900nm
  float calibratedL = sensor.getCalibratedL();

//   sensor.getA(); //410nm
//   sensor.getB(); //435nm
//   sensor.getC(); //460nm
//   sensor.getD(); //485nm
//   sensor.getE(); //510nm
//   sensor.getF(); //535nm
//   sensor.getG(); //560nm
//   sensor.getH(); //585nm
//   sensor.getR(); //610nm
//   sensor.getI(); //645nm
//   sensor.getS(); //680nm
//   sensor.getJ(); //705nm
//   sensor.getT(); //730nm
//   sensor.getU(); //760nm
//   sensor.getV(); //810nm
//   sensor.getW(); //860nm
//   sensor.getK(); //900nm
  
//  float uncalibratedL = sensor.getL();

 sensor.disableBulb(AS7265x_LED_IR);
  
  
  Serial.print(calibratedD); //485nm
  Serial.print(",");
  Serial.print(calibratedI); //645nm
  Serial.print(",");
  Serial.print(calibratedW); //860nm
  Serial.print(",");
  Serial.println(calibratedL); //940nm

// Works when a BT message is sent to Pico W
 if (SerialBT.available()) {
   Serial.println("BT Available");
   SerialBT.write("Reading:");
   SerialBT.write(sensor.getCalibratedL());
 }
    SerialBT.print("Reading:");
    SerialBT.println(calibratedW);


 delay(2000);

}