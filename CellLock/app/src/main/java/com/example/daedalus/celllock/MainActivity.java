package com.example.daedalus.celllock;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;


import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
    private static final int INIT_STATE = 5;
    private static final int CONNECTED_STATE = 6;
    private static final int PROTECTED_STATE = 7;
    private static final int FINDARDUINO_STATE = 8;
    private static final int WARNING_STATE = 9;
    private static final int DANGER_STATE = 10;
    private static final int DISCONNECTED_STATE = 11;
    public int prevState = 0;
    public static MediaPlayer mPlayer;

    private static final String CONNECTED_DEVICE_NAME = "Peinaw";
    private static final String DISCOVERY_DEVICE_NAME = "Orestis";
    private BluetoothDevice connectionDevice;
    //private BluetoothDevice discoveryDevice;
    private ConnectedThread connectedThread;
    private Thread resendStateThread;

    TextView textView;
    String tag = "debugging";


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Log.i(tag, "in handler");
            super.handleMessage(msg);
            switch (msg.what) {
                case SUCCESS_CONNECT:
                    // when you connect to a device restart discovery in order to search for other
                    // devices
                    connectedThread = new ConnectedThread((BluetoothSocket) msg.obj);

                    // State changes to Connected State;
                    changeState(CONNECTED_STATE);


                    //String temp = connectedThread.readData(getApplicationContext());

                    //if(temp==null){
                    //     changeState(DISCONNECTED_STATE);
                    //  }

                    //Check thn malakia mas
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String string = new String(readBuf);
                    Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendStateThread != null) {
            resendStateThread.interrupt();
            resendStateThread = null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        textView = (TextView) findViewById(R.id.debuggText);
        init();

        if (btAdapter == null) {
            Toast.makeText(getApplicationContext(), "No bluetooth detected", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!btAdapter.isEnabled()) {
                turnOnBT();
            }
        }

        // start thread that resends current state command repeatedly.
        resendStateThread = new Thread(new Runnable() {
            public void run() {
                Map<Integer, String> dict = new HashMap<Integer, String>();
                // uncomment if you want to send "PON;" through this thread.
//                dict.put(PROTECTED_STATE, "PON;");
                dict.put(WARNING_STATE, "SEMI;");
                dict.put(DANGER_STATE, "DANG;");
                dict.put(CONNECTED_STATE, "POFF;");

                while (true) {
                    final String stateCommand = dict.get(state);
                    if (connectedThread != null && stateCommand != null) {
                        connectedThread.write(stateCommand.getBytes());
                    }

                    SystemClock.sleep(500);
                }
            }
        });
        resendStateThread.start();
    }

    private void playAlarm() {
        mPlayer.start();
    }

    private void startDiscovery() {
        // TODO Auto-generated method stub
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();

    }

    private void getPairedDevices() {
        // TODO Auto-generated method stub
        devicesArray = btAdapter.getBondedDevices();
        if (devicesArray.size() > 0) {
            for (BluetoothDevice device : devicesArray) {
                pairedDevices.add(device.getName());

            }
        }
    }

    // Rendering Functions
    public void renderToProtected() {
        ImageView imgShield = (ImageView) findViewById(R.id.imageViewShield);
        ImageView imgDanger = (ImageView) findViewById(R.id.imageViewDanger);
        ImageView imgWarning = (ImageView) findViewById(R.id.imageViewWarning);
        ImageView imgBell = (ImageView) findViewById(R.id.imageViewBell);
        imgBell.setVisibility(ImageView.INVISIBLE);
        imgWarning.setVisibility(ImageView.INVISIBLE);
        imgDanger.setVisibility(ImageView.INVISIBLE);
        imgShield.setVisibility(ImageView.VISIBLE);
    }

    public void renderToDanger() {
        ImageView imgShield = (ImageView) findViewById(R.id.imageViewShield);
        ImageView imgDanger = (ImageView) findViewById(R.id.imageViewDanger);
        ImageView imgWarning = (ImageView) findViewById(R.id.imageViewWarning);
        ImageView imgBell = (ImageView) findViewById(R.id.imageViewBell);
        imgWarning.setVisibility(ImageView.INVISIBLE);
        imgBell.setVisibility(ImageView.INVISIBLE);
        imgShield.setVisibility(ImageView.INVISIBLE);
        imgDanger.setVisibility(ImageView.VISIBLE);

    }

    public void renderToWarning() {
        ImageView imgShield = (ImageView) findViewById(R.id.imageViewShield);
        ImageView imgDanger = (ImageView) findViewById(R.id.imageViewDanger);
        ImageView imgWarning = (ImageView) findViewById(R.id.imageViewWarning);
        ImageView imgBell = (ImageView) findViewById(R.id.imageViewBell);
        imgBell.setVisibility(ImageView.INVISIBLE);
        imgDanger.setVisibility(ImageView.INVISIBLE);
        imgShield.setVisibility(ImageView.INVISIBLE);
        imgWarning.setVisibility(ImageView.VISIBLE);
    }

    public void renderToBell() {
        ImageView imgShield = (ImageView) findViewById(R.id.imageViewShield);
        ImageView imgDanger = (ImageView) findViewById(R.id.imageViewDanger);
        ImageView imgWarning = (ImageView) findViewById(R.id.imageViewWarning);
        ImageView imgBell = (ImageView) findViewById(R.id.imageViewBell);
        imgWarning.setVisibility(ImageView.INVISIBLE);
        imgShield.setVisibility(ImageView.INVISIBLE);
        imgDanger.setVisibility(ImageView.INVISIBLE);
        imgBell.setVisibility(ImageView.VISIBLE);

    }
    // End of Rendering Functions

    // Initialization method
    private void init() {
        if (MainActivity.mPlayer != null) {
            MainActivity.mPlayer.stop();
            MainActivity.mPlayer = null;
        }

        // Initialize the first state
        mPlayer = MediaPlayer.create(getApplication(), R.raw.alarm);
        mPlayer.setLooping(true);
        state = INIT_STATE;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = new ArrayList<String>();
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int rssi;
                // TODO Auto-generated method stub
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (state == INIT_STATE) {
                        if (device.getName().equals(CONNECTED_DEVICE_NAME)) {
                            connectionDevice = device;
                            Toast.makeText(getApplicationContext(), " Device for Connection Found ", Toast.LENGTH_SHORT).show();

                            if (pairedDevices.contains(connectionDevice.getName())) {

                                // Start Connection
                                ConnectThread connect = new ConnectThread(connectionDevice);
                                connect.start();

                            } else {
                                Toast.makeText(getApplicationContext(), " You MUST be paired with the  " + CONNECTED_DEVICE_NAME, Toast.LENGTH_SHORT).show();

                            }
                        }
                    } else if (state == PROTECTED_STATE || state == WARNING_STATE) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        String temp = connectedThread.readData(getApplicationContext());
                        // textView.setText(temp);

                        if (temp == null) {
                            changeState(DISCONNECTED_STATE);
                        } else if (temp.contains("RING")) {
                            playAlarm();
                        }

                        if (device.getName().equals(DISCOVERY_DEVICE_NAME)) {
                            //discoveryDevice = device;
                            Toast.makeText(getApplicationContext(), " Discovery device found ", Toast.LENGTH_SHORT).show();
                            rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                            textView.setText("" + rssi);
                            //Send rssi
                            // Get response
                            int response = 0; // the responce from tsiri
                            if (rssi < -75) {
                                response = 2;
                            }
                            if (response == 0) {
                                changeState(PROTECTED_STATE);
                            } else if (response == 1) {
                                changeState(WARNING_STATE);
                            } else if (response == 2) {
                                changeState(DANGER_STATE);

                            }
                            // Change state


                            //Toast.makeText(getApplicationContext(), rssi + " dB", Toast.LENGTH_SHORT).show();
                            btAdapter.cancelDiscovery();

                        }
                    }

                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    //Toast.makeText(getApplicationContext(), " DiscoveryStarted ", Toast.LENGTH_SHORT).show();
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    // VERY VERY IMPORTANT
                    if (state == PROTECTED_STATE || state == WARNING_STATE) {
                        btAdapter.startDiscovery();
                    }


                } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    if (btAdapter.getState() == btAdapter.STATE_OFF) {
                        turnOnBT();
                    }
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    changeState(DISCONNECTED_STATE);
                    //Device has disconnected
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
        filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        registerReceiver(receiver, filter);
    }


    private void changeState(int s) {
        int temp = prevState;
        prevState = state;
        state = s;
        if (state == PROTECTED_STATE) {
            renderToProtected();
            connectedThread.write("PON;".getBytes());
        } else if (state == CONNECTED_STATE) {
            renderToWarning();
            connectedThread.write("POFF;".getBytes());
        } else if (state == WARNING_STATE) {
            renderToWarning();
            connectedThread.write("SEMI;".getBytes());
        } else if (state == DANGER_STATE) {

            playAlarm();
            connectedThread.write("DANG;".getBytes());
            final Intent intent = new Intent(this, LockActivity.class);
            renderToDanger();
            connectedThread.cancel();
            startActivity(intent);

        } else if (state == DISCONNECTED_STATE) {
            //&& temp != CONNECTED_STATE && temp !=INIT_STATE
            changeState(DANGER_STATE);

        }
        //textView.clearComposingText();
        //  textView.setText(""+state);

    }


    //Enable bluetooth function

    private void turnOnBT() {
        // TODO Auto-generated method stub
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, 1);
    }


    public void connectButton(View view) {
        getPairedDevices();
        startDiscovery();


    }


    public void protectionButton(View view) {
        if (state == INIT_STATE) {
            Toast.makeText(getApplicationContext(), "You MUST connect to get on protection mode", Toast.LENGTH_LONG).show();

        } else if (state == CONNECTED_STATE) {
            changeState(PROTECTED_STATE);
            Toast.makeText(getApplicationContext(), "Protection Mode Enabled", Toast.LENGTH_LONG).show();

            // start discovery mode
            startDiscovery();
        } else if (state == PROTECTED_STATE) {
            Toast.makeText(getApplicationContext(), "You are in protection mode", Toast.LENGTH_LONG).show();
        }
    }


    public void disableProtection(View view) {
        if (state == PROTECTED_STATE) {
            changeState(CONNECTED_STATE);
            btAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(), "You are not protected anymore", Toast.LENGTH_LONG).show();
        } else {

        }
    }

    public void ringButton(View view) {

        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        } else if (state == PROTECTED_STATE || state == CONNECTED_STATE) {
            String s = "RING;";
            connectedThread.write(s.getBytes());
            renderToBell();
            changeState(FINDARDUINO_STATE);


        } else if (state == INIT_STATE) {
            textView.clearComposingText();
            Toast.makeText(getApplicationContext(), "You must Connect first to find your keys", Toast.LENGTH_SHORT).show();
        } else if (state == FINDARDUINO_STATE) {
            String s = "RSTOP;";
            connectedThread.write(s.getBytes());
            Toast.makeText(getApplicationContext(), "Congrats you found your fucking keys", Toast.LENGTH_SHORT).show();
            changeState(prevState);
        }

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
            } catch (IOException connectException) {
                Log.i(tag, "connect failed");
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                }
                return;
            }

            // Do work to manage the connection (in a separate thread)

            mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
        }


        /**
         * Will cancel an in-progress connection, and close the socket
         */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
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
            } catch (IOException e) {
            }

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
            } catch (IOException e) {
            }
        }


        public String read(byte[] bytes) {

            strInput = null;
            try {
                mmInStream.read(bytes);
                strInput = new String(bytes);
                //strInput = new String(bytes);
            } catch (IOException e) {
                changeState(DISCONNECTED_STATE);
            }
            return strInput;
        }


        private boolean isBluetoothAvailable() {
            return btAdapter.isEnabled();

        }

        private void turnBluetoothOn() {

            btAdapter.enable();
        }

        public String readData(Context context) {
            String outputString = null;
            if (isBluetoothAvailable()) {
                outputString = connectedThread.read(buffer);
            } else {
                Toast.makeText(context, "Error: Not Connected", Toast.LENGTH_LONG).show();
            }
            return outputString;
        }


        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }
}



