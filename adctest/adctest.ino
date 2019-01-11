int sensorPin=A0;                              // Variable to get amplified sensor data
int sensorValue=0;                             // Variable to store the sensor data
float volt;                                    // Variable to display digital data
float volt1;                                   // Variable to store the digital data in Volts
void setup() 
{
  Serial.begin(9600);                          // Begins serial communication at a baud rate of 9600
}

void loop()
{
 sensorValue= analogRead(sensorPin);           // Read amplified sensor data   
 volt=(sensorValue*5.0/1023);                  // Digital to Analog conversion
 volt1=volt*1000;                              // Millivolts to Volts conversion
 Serial.println (volt1);                       // To print final voltage value
 delay (1000);                                 // Delay time after which next sensor read happens                           
}
