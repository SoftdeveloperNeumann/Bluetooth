package de.example.frank.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {


    private List<BluetoothDevice> mArrayAdapter ;
    private TextView textview,input,output;
    private Button connect,senden;
    private BroadcastReceiver btReceiver=null;
    private Handler handler;
    private BluetoothAdapter adapter;
    private BluetoothService bts;
    private String mConnectedDeviceName = null;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    public static String EXTRA_DEVICE_ADDRESS = "device_address";
//    private ArrayAdapter<String> mConversationArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textview = (TextView)findViewById(R.id.textview);
        input = (TextView) findViewById(R.id.input);
        output = (TextView) findViewById(R.id.output);
        connect = (Button) findViewById(R.id.connect);
        senden = (Button) findViewById(R.id.senden);
        adapter = BluetoothAdapter.getDefaultAdapter();
        mArrayAdapter = new ArrayList<BluetoothDevice>();
        /**
         * The Handler that gets information back from the BluetoothChatService
         */
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Activity activity = getParent();
                switch (msg.what) {
                    case Constants.MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case BluetoothService.STATE_CONNECTED:
                                setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                                mArrayAdapter.clear();
                                break;
                            case BluetoothService.STATE_CONNECTING:
                                setStatus(R.string.title_connecting);
                                break;
                            case BluetoothService.STATE_LISTEN:
                            case BluetoothService.STATE_NONE:
//                                setStatus(R.string.title_not_connected);
                                break;
                        }
                        break;
                    case Constants.MESSAGE_WRITE:
                        byte[] writeBuf = (byte[]) msg.obj;
                        // construct a string from the buffer
                        String writeMessage = new String(writeBuf);
                        output.append("Me:  " + writeMessage);
                        break;
                    case Constants.MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        // construct a string from the valid bytes in the buffer
                        String readMessage = new String(readBuf, 0, msg.arg1);
                        output.append(mConnectedDeviceName + ":  " + readMessage);
                        break;
                    case Constants.MESSAGE_DEVICE_NAME:
                        // save the connected device's name
                        mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                        if (null != activity) {
                            Toast.makeText(activity, "Connected to "
                                    + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case Constants.MESSAGE_TOAST:
                        if (null != activity) {
                            Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                    Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        };

        checkBluetooth();
        ensureDiscoverable();

        bts = new BluetoothService(this,handler);
        bts.start();
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mArrayAdapter.size()>0) {
                    BluetoothDevice theDevice =mArrayAdapter.get(0);
                    input.setText(theDevice.getName() + " - " + theDevice.getAddress() + bts.getState());
                    mConnectedDeviceName = theDevice.getName();
                    bts.connect(theDevice,true);


                }
            }
        });
        senden.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = input.getText().toString();
                sendMessage(str);
            }
        });

        btReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    textview.append("ein neues Gerät wurde gefunden");
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(!mArrayAdapter.contains(device)) {
                        mArrayAdapter.add(device);
                    }
                }if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    textview.append("fertig mit suchen: ");
                    textview.append(""+mArrayAdapter.size() + "\n");

                    Iterator it = mArrayAdapter.iterator();
                    while(it.hasNext()){
                        BluetoothDevice name = (BluetoothDevice) it.next();
                        textview.append(name.getName() + " // " + name.getAddress());
                    }

                }

            }
        };
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(btReceiver,filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(btReceiver,filter);

        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
// If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                if(!mArrayAdapter.contains(device)) {
                    mArrayAdapter.add(device);
                }
            }
        }

        checkPermissions();
        adapter.startDiscovery();


    }

    /**
     * Called after {@link #onRestoreInstanceState}, {@link #onRestart}, or
     * {@link #onPause}, for your activity to start interacting with the user.
     * This is a good place to begin animations, open exclusive-access devices
     * (such as the camera), etc.
     * <p>
     * <p>Keep in mind that onResume is not the best indicator that your activity
     * is visible to the user; a system window such as the keyguard may be in
     * front.  Use {@link #onWindowFocusChanged} to know for certain that your
     * activity is visible to the user (for example, to resume a game).
     * <p>
     * <p><em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em></p>
     *
     * @see #onRestoreInstanceState
     * @see #onRestart
     * @see #onPostResume
     * @see #onPause
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (bts != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (bts.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                bts.start();
            }
        }
    }

    private void setStatus(int resId) {
        Activity activity = this;
        if (null == activity) {
            return;
        }
//        final ActionBar actionBar = activity.getActionBar();
//        if (null == actionBar) {
//            return;
//        }
//        actionBar.setSubtitle(resId);
    }

    private void setStatus(CharSequence subTitle) {
        Activity activity = this;
        if (null == activity) {
            return;
        }
//        final ActionBar actionBar = activity.getActionBar();
//        if (null == actionBar) {
//            return;
//        }
//        actionBar.setSubtitle(subTitle);
    }

    private void ensureDiscoverable() {
        if(adapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent sichtbar = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            sichtbar.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
            startActivity(sichtbar);
        }
    }

    private void checkBluetooth() {
        if(adapter == null){
            // kein Bluetooth
            finish();
        }
        if( !adapter.isEnabled()){
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT,REQUEST_ENABLE_BT);
        }
    }

    private void checkPermissions() {
        int bt = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH);
        int btadmin = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_ADMIN);
        int btCoars = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        int btFine = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (bt+btadmin+btCoars+btFine != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.BLUETOOTH},
                    2);
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.BLUETOOTH_ADMIN},
                    2);
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    2);
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    2);
            System.out.println("werte gesetzt");
//            adapter.startDiscovery();


        }else {
            System.out.println("werte  waren gesetzt gesetzt");
//            adapter.startDiscovery();
        }
    }

    public void onActivityResult(int requestCode,int resultCode, Intent data){
        textview.setText(" der requestcode ist " + requestCode + "\n" );
        textview.append("der resultcode ist " + resultCode + "\n");
        if(data !=null) {
            textview.append(data.getAction());
        }
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;

        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = adapter.getRemoteDevice(address);
        // Attempt to connect to the device
        bts.connect(device, secure);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int [] grantResults){
        switch (requestCode) {
            case 2: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    continueDoDiscovery();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        shouldShowRequestPermissionRationale(permissions[0]);
                    }
                    Toast.makeText(this,
                            "Permission fehler",
                            Toast.LENGTH_LONG).show();
//                    cancelOperation();
                }
                return;
            }

        }
    }

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (bts.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            bts.write(send);

        }
    }

    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(btReceiver);
    }

}
