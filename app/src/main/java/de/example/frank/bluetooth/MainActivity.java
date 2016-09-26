package de.example.frank.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_ENABLE_BT = 123;
    private Map mArrayAdapter ;
    private TextView textview;
    private BroadcastReceiver btReceiver=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textview = (TextView)findViewById(R.id.textview);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        mArrayAdapter = new HashMap();
        if(adapter == null){
            // kein Bluetooth
        }
        if( !adapter.isEnabled()){
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT,REQUEST_ENABLE_BT);
        }
        Intent sichtbar = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivity(sichtbar);

        btReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    textview.append("ein neues Gerät wurde gefunden");
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mArrayAdapter.put(device.getName(),device.getAddress());
                }if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    textview.append("fertig mit suchen");
                    textview.append(""+mArrayAdapter.size());
                    Set keys = mArrayAdapter.keySet();
                    Iterator it = keys.iterator();
                    while(it.hasNext()){
                        String name = it.next().toString();
                        textview.append(name + " // " + mArrayAdapter.get(name));
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
                mArrayAdapter.put(device.getName(),device.getAddress());
            }
        }

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
            adapter.startDiscovery();


        }else {
            System.out.println("werte  waren gesetzt gesetzt");
            adapter.startDiscovery();
        }



    }
    public void onActivityResult(int requestCode,int resultCode, Intent data){
        textview.setText(" der requestcode ist " + requestCode + "\n" );
        textview.append("der resultcode ist " + resultCode + "\n");
        if(data !=null) {
            textview.append(data.getAction());
        }
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
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(btReceiver);
    }

}
