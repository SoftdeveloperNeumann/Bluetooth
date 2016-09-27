package de.example.frank.bluetooth;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

/**
 * Created by Administrator on 26.09.2016.
 */

public class AcceptThread extends Thread {
    private final BluetoothServerSocket serverSocket ;

    public AcceptThread(BluetoothServerSocket btSS){
        serverSocket = btSS;
    }

    public void run(){
        BluetoothSocket socket = null;
        while(true){
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(socket!=null){
                new ConnectedThread(socket);
            }
        }

    }

}
