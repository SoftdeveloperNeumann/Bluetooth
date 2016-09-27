package de.example.frank.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Administrator on 26.09.2016.
 */
public class ConnectThread extends Thread {
    private final static String LOG = ConnectThread.class.getSimpleName();

    private final BluetoothSocket socket;
    private final BluetoothDevice device;

    public ConnectThread(BluetoothDevice d){
        BluetoothSocket tmp = null;
        device = d;
        try {
            tmp = device.createRfcommSocketToServiceRecord(device.getUuids()[0].getUuid());
        } catch (IOException e) {
            Log.e(LOG, e.getMessage());
            e.printStackTrace();
        }
    socket = tmp;

    }
    public void run (){
        try {
            socket.connect();
        } catch (IOException e) {
            Log.e(LOG, e.getMessage());
            e.printStackTrace();
            cancel();
        }
    }
    public void cancel(){
        try {
            socket.close();
        } catch (IOException e) {
            Log.e(LOG, e.getMessage());
            e.printStackTrace();
        }
    }
}
