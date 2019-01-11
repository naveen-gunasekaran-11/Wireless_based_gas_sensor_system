package com.example.naveeng.sensinonav1;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;


public class MainActivity extends Activity {
    private static final String TAG = "bluetooth2";
    float V2 = 0.0f;
    float V3 = 0.0f;
    float V1 = 1010.010f;
    int i = 0, j = 0;
    float[] a = new float[5000];
    long[] b = new long[5000];
    Button getdata,store;
    TextView txtArduino,volt_1,volt_2,volt_3,sensitivity_dt,response_time,recovery_time,vapor_dt;
    Handler h;
    long millis1 = 0;
    long millis2 = 0;
    final int RECIEVE_MESSAGE = 1;                                                                                         // Status  for Handler
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder sb = new StringBuilder();
    private ConnectedThread mConnectedThread;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");                           // SPP UUID service

    private static String address = "98:D3:31:80:77:6A";                                                                   // MAC-address of Bluetooth module

    FileWriter out;

      @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getdata = (Button) findViewById(R.id.button);                                                                       // Button to receive data
        store= (Button) findViewById(R.id.button2);                                                                         // Button to store data 
        txtArduino = (TextView) findViewById(R.id.textView);                                                                // Textbox to display sensor data
        volt_1 = (TextView) findViewById(R.id.textView2);                                                                   // Textbox to display baseline voltage
		volt_2 = (TextView) findViewById(R.id.textView4);                                                                   // Textbox to display response voltage
        volt_3 = (TextView) findViewById(R.id.textView6);                                                                   // Textbox to display recovery voltage
        sensitivity_dt = (TextView) findViewById(R.id.textView3);                                                           // Textbox to display sensitivity
        response_time = (TextView) findViewById(R.id.textView5);                                                            // Textbox to display response time
        recovery_time = (TextView) findViewById(R.id.textView7);                                                            // Textbox to display recovery time
        vapor_dt = (TextView) findViewById(R.id.textView8);                                                                 // Textbox to display vapor detected



        try {
            out = new FileWriter(new File(getApplicationContext().getExternalFilesDir(null).getAbsolutePath(), "sensor.txt"));//initialization and creation of the file
        } catch (IOException e) {
            e.printStackTrace();
        }

        
        h = new Handler() {

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                                    							    // if receive message
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);                                                 // create string from bytes array
                        sb.append(strIncom);                                               								    // append string
                        int endOfLineIndex = sb.indexOf("\r\n");                                                            // determine the end-of-line
                        if (endOfLineIndex > 0) {                                                                           // if end-of-line,
                            String sbprint = sb.substring(0, endOfLineIndex);                                               // extract string and clear
                            sb.delete(0, sb.length());                                                                     
                            txtArduino.setText("Data: " + sbprint + " mV");                                                 // update TextView

                            float f = Float.parseFloat(sbprint);
                            a[i] = f;
                            b[i] = System.currentTimeMillis();
                            i++;

                            if(f >= V2) {
                                V2 = f;
                            }
							V3 = f;
                            
							try {
                                out.append(sbprint);                                                                        // write the string to the file
                                out.append("\n");
                            } 
							catch (IOException ioe) {
                                ioe.printStackTrace();
                            }
						}                    
                        break;
                }
            };
        };


        btAdapter = BluetoothAdapter.getDefaultAdapter();                                                                   // Get Bluetooth adapter
        checkBTState();

        getdata.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                getdata.setEnabled(false);                                                                                  // Start to receive data on button click 
                mConnectedThread.start();
            }
        });

        store.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    out.close();                                                                                            // Close the file
                    store.setEnabled(false);                               													// Store the acquired data
                } 
				catch(IOException ioe1) {
                    ioe1.printStackTrace();
                }
				calculator();                                                                                               // Call function calculator on clicking store button
            }
        });
    }

    public void calculator()
    {
        int start = 0, stop = 0, start2 = 0, stop2 = 0;

        for(j = 0; j < i; j++) {                                                                                            // Calculation of stop of response time
            float k = 0.9f*V2;
            if(k >= a[j] && k <= a[j+1]){
                if(a[j+1] - k > k - a[j]) {
                    k = a[j];
                    stop = j;
                }
                else {
                    k = a[j + 1];
                    stop = j + 1;
                }
                break;
            }
        }
        for(j = 0; j < i; j++) {                                                                                            // Calculation of start of response time
            float k = 1.1f*V1;
            if(k >= a[j] && k <= a[j+1]){
                if(a[j+1] - k > k - a[j]) {
                    k = a[j];
                    start=j;
                }
                else {
                    k = a[j + 1];
                    start = j + 1;
                }
                break;
            }
        }

        for(j = 0; j < i; j++) {                                                                                            // Calculation of start of recovery time
            float k = 0.9f*V2;
            if(k <= a[j] && k >= a[j+1]){
                if(k - a[j+1] > a[j] - k) {
                    k = a[j];
                    start2=j;
                }
                else {
                    k = a[j + 1];
                    start2 = j + 1;
                }
            }
        }

        for(j = 0; j < i; j++) {                                                                                            // Calculation of stop of recovery time
            float k = 1.1f*V3;
            if(k <= a[j] && k >= a[j+1]){
                if(k - a[j+1] > a[j] - k) {
                    k = a[j];
                    stop2 = j;
                }
                else {
                    k = a[j + 1];
                    stop2 = j + 1;
                }
            }
        }
        
		long responsetime = (b[stop] - b[start])/1000;                                                                      // Calculation of response time
        long recoverytime = (b[stop2] - b[start2])/1000;                                                                    // Calculation of recovery time
        float sensitivity = V2/V1;                                                                                          // Calculation of sensitivity

        volt_1.setText("v1="+ V1 + " mV");                                                                                  // Display V1
        volt_2.setText("v2="+ V2 + " mV");                                                                                  // Display V2
        volt_3.setText("v3="+ V3 + " mV");                                                                                  // Display V3
        sensitivity_dt.setText( String.format( "Sensitivity: %.2f", sensitivity ) );                                        // Display sensitivity
        response_time.setText("Response Time=" + responsetime + " s");                                                      // Display response time
        recovery_time.setText("Recovery Time=" + recoverytime + " s");                                                      // Display recovery time

        if (sensitivity < 3.0f)                                                                                             // Display of vapor detected depending on sensitivity
            vapor_dt.setText("Vapor Detected is Formaldehyde");
        else if (sensitivity >3.0f && sensitivity <8.15f)
            vapor_dt.setText("Vapor Detected is Ammonia");
        else if (sensitivity >8.15f)
            vapor_dt.setText("Vapor Detected is TMA");

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {                              // Function to create Bluetooth Socket
        if (Build.VERSION.SDK_INT >= 10) {
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class});
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection", e);
            }
        }
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");
        BluetoothDevice device = btAdapter.getRemoteDevice(address);                                                        // Set up a pointer to the remote node using it's address.

        try {
            btSocket = createBluetoothSocket(device);                                                                       // Create bluetooth socket
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        btAdapter.cancelDiscovery();

        Log.d(TAG, "...Connecting...");                                                                                     // Establish the connection
        try {
            btSocket.connect();
            Log.d(TAG, "....Connection ok...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        Log.d(TAG, "...Create Socket...");                                                                                  // Create a data stream so we can talk to server.

        mConnectedThread = new ConnectedThread(btSocket);
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        try {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {                                                                                           // Check for Bluetooth support and then check to make sure it is turned on
        
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {                                                                                                        //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }


    private void errorExit(String title, String message) {
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }



    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
			
            try {                                                                                                           // Get the input and output streams, using temp objects
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];                                                                                   // Buffer store for the stream
            int bytes;                                                                                                       // bytes returned from read()

            while (true) {                                                                                                   // Keep listening to the InputStream until an exception occurs
                try {                                                                                                        // Read from the InputStream
                    bytes = mmInStream.read(buffer);                                                                         // Get number of bytes and message in "buffer"
                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();                                      // Send to message queue Handler
                } catch (IOException e) {
                    break;
                }
            }
        }

    }
}