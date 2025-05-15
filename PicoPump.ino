#include <SerialBT.h>

//BluetoothSerial SerialBT;
const char* deviceName = "PicoW-Belt";

void setup() {
  Serial.begin(115200);
  SerialBT.setName("PicoW-Belt");
  SerialBT.begin(); // Start Bluetooth with the device name
  Serial.println("Bluetooth Device Ready! Connect with: " + String(deviceName));
  pinMode(15, OUTPUT); // Set GPIO 15 as output
  pinMode(LED_BUILTIN, OUTPUT);
}

void loop() {
  // data received from a connected client.
  digitalWrite(LED_BUILTIN, HIGH); 
  if (SerialBT.available()) {
    String clientMessage = SerialBT.readStringUntil('\n');
    Serial.print("Client says: ");
    Serial.println(clientMessage);
    float InjectionTime = clientMessage.toFloat();


   if (InjectionTime != 0) {
    digitalWrite(15, HIGH); // Set pin HIGH
    delay(InjectionTime);            // Wait 1 second
    digitalWrite(15, LOW);  // Set pin LOW
   } 
    
    // Example: Sending a response back to the client
    String serverResponse = "Server received: " + clientMessage;
    SerialBT.println(serverResponse);

  } 
  delay(10); // Small delay to prevent busy-waiting
}