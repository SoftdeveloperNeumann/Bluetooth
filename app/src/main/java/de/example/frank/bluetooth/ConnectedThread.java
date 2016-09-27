package de.example.frank.bluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 26.09.2016.
 */
public class ConnectedThread extends Thread{
    private final BluetoothSocket socket;
    private final InputStream input;
    private final OutputStream output;

    public ConnectedThread(BluetoothSocket socket) {
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        this.socket = socket;

        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        input = tmpIn;
        output = tmpOut;
    }
    public void write(byte[] bytes){
        try {
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void cancel(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
