The folder Wireless_based_gas_sensor_system  has two sub folders namely adctest and Sensinonav1. 

The first folder adctest contains the C code that runs on the Arduino UNO board. This program reads the sensor value continously and transmits the data wirelessly
via a bluetooth module that is connected to it.

The second folder Sensinonav1 contains the Android application that can be installed on any android device. This JAVA application establishes a connection between the 
application and the bluetooth device which is connected to the Arduino board. Once a connection is established, the data is received by the application and is
displayed on the application screen.
