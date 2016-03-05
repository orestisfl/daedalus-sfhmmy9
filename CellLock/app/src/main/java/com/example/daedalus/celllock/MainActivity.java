package com.example.daedalus.celllock;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {


    BluetoothAdapter btAdapter;
    ArrayList<String> pairedDevices;
    Set<BluetoothDevice> devicesArray;
    BroadcastReceiver receiver;
    IntentFilter filter;
    private int state;
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    protected static final int SUCCESS_CONNECT = 0;
    protected static final int MESSAGE_READ = 1;
    private static final int  INIT_STATE = 5;
    private static final String CONNECTED_DEVICE_NAME ="ERCOMACTE";
    private static final String DISCOVERY_DEVICE_NAME ="Orestis";
    private BluetoothDevice connectionDevice;
    private BluetoothDevice discoveryDevice;
    private ConnectedThread connectedThread;
    String tag = "debugging";



    Handler mHandler = new Handler()  {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Log.i(tag, "in handler");
            super.handleMessage(msg);
            switch(msg.what){
                case SUCCESS_CONNECT:
                    // when you connect to a device restart discovery in order to search for other
                    // devices
                    btAdapter.startDiscovery();



                    connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
                    Toast.makeText(getApplicationContext(), "CONNECT", Toast.LENGTH_SHORT).show();
                    String s = "successfully connected";
                    connectedThread.write(s.getBytes());

                    // Check thn malakia mas

                    String t = "1";
                    connectedThread.write(t.getBytes());
                    try {
                        Thread.sleep(4000);
                        t = "0";
                        connectedThread.write(t.getBytes());
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }



                    //S Log.i(tag, "connected");
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[])msg.obj;
                    String string = new String(readBuf);
                    Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        init();
        if(btAdapter==null){
            Toast.makeText(getApplicationContext(), "No bluetooth detected", Toast.LENGTH_SHORT).show();
            finish();
        }
        else {
            if (!btAdapter.isEnabled()) {
                turnOnBT();
            }
        // Start the main app


        }
            //startDiscovery();
        //


    }



    private void startDiscovery() {
        // TODO Auto-generated method stub
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();

    }

    private void getPairedDevices() {
        // TODO Auto-generated method stub
        devicesArray = btAdapter.getBondedDevices();
        if(devicesArray.size()>0){
            for(BluetoothDevice device:devicesArray){
                pairedDevices.add(device.getName());

            }
        }
    }


    // Initialization method

    private void init() {

        // Initialize the first state
        state = INIT_STATE;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = new ArrayList<String>();
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        receiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {

                // TODO Auto-generated method stub
                String action = intent.getAction();

                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(state==INIT_STATE){
                        Log.i(tag, "MALAKIA");
                        if(device.getName().equals(CONNECTED_DEVICE_NAME)){
                            Log.i(tag, "MALAKIA1");

                            Log.i(tag, "MALAKIA2");
                            connectionDevice = device;
                            Log.i(tag, "MALAKIA3");
                            Toast.makeText(getApplicationContext()," Device for Connection Found " , Toast.LENGTH_SHORT).show();

                            if(pairedDevices.contains(connectionDevice)){
                                // Start Connection
                                ConnectThread connect = new ConnectThread(connectionDevice);
                                connect.start();
                                Log.i(tag, "MALAKIA4");
                            }
                            else{
                                Toast.makeText(getApplicationContext()," You MUST be paired with the  " + CONNECTED_DEVICE_NAME , Toast.LENGTH_SHORT).show();

                            }
                        }
                    }









                   // int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);

                   /* Toast.makeText(getApplicationContext(),"  RSSI: " + rssi + " dBm " + device.getName() , Toast.LENGTH_SHORT).show();

                    //devices.add(device);
                    String s = "";
                    for(int a = 0; a < pairedDevices.size(); a++){
                        if(device.getName().equals(pairedDevices.get(a))){
                            //append
                            s = "(Paired)";
                            break;
                        }
                    }
*/
                    //listAdapter.add(device.getName()+" "+s+" "+"\n"+device.getAddress());
                    // Do not delete this
                   // btAdapter.cancelDiscovery();

                }

                else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){

                    Toast.makeText(getApplicationContext(), " DiscoveryStarted ", Toast.LENGTH_SHORT).show();
                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    // VERY VERY IMPORTANT
                    btAdapter.startDiscovery();



                }
                else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                    if(btAdapter.getState() == btAdapter.STATE_OFF){
                        turnOnBT();
                    }
                }

            }
        };

        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter);
    }





    //Enable bluetooth function

    private void turnOnBT() {
        // TODO Auto-generated method stub
        Intent intent =new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, 1);
    }


    public void connectButton(){

        startDiscovery();


    }



// Code from Android develope bluetooth app

    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;
            Log.i(tag, "construct");
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.i(tag, "get socket failed");

            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            btAdapter.cancelDiscovery();
            Log.i(tag, "connect - run");
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                Log.i(tag, "connect - succeeded");
            } catch (IOException connectException) {	Log.i(tag, "connect failed");
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)

            mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
        }



        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }


    // Code from Android develope bluetooth app

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private String strInput;
        private byte[] buffer = new byte[1024];

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            //run();
        }

        public void run() {
            byte[] buffer;  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    buffer = new byte[1024];
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();

                    //listAdapter.add(new String(buffer));

                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }


        public String read(byte[] bytes){
            try {
                mmInStream.read(bytes);
                strInput = new String(bytes);
            }catch(Exception e){
                e.printStackTrace();
            }
            return strInput;
        }


        private boolean isBluetoothAvailable(){
            return btAdapter.isEnabled();

        }
        private void turnBluetoothOn(){

            btAdapter.enable();
        }

        public String readData(Context context){
            String outputString=null;
            if(isBluetoothAvailable()) {
                outputString = connectedThread.read(buffer);
            }else{
                Toast.makeText(context, "Error: Not Connected", Toast.LENGTH_LONG).show();
            }
            return outputString;
        }


        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}



