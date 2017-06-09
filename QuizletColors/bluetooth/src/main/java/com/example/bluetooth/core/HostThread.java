package com.example.bluetooth.core;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

/**
 * Created by rebeccastecker on 6/8/17.
 */
class HostThread extends Thread {
    public static final String TAG = HostThread.class.getSimpleName();
    private BluetoothStuff bluetoothStuff;
    private final BluetoothServerSocket mmServerSocket;

    public HostThread(BluetoothStuff bluetoothStuff) {
        this.bluetoothStuff = bluetoothStuff;
        BluetoothServerSocket tmp = null;
        try {
            String name = "sharks";
            // MY_UUID is the app's UUID string, also used by the client code.
            tmp = bluetoothStuff.mBluetoothAdapter.listenUsingRfcommWithServiceRecord(name, BluetoothStuff.APP_UUID);
        } catch (IOException e) {
            Log.e(BluetoothStuff.TAG, "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
    }

    public void run() {
        // Keep listening until exception occurs
        while (true) {
            BluetoothSocket socket = null;
            try {
                /*
                Note that when accept() returns the BluetoothSocket, the socket is already
                connected. Therefore, you shouldn't call connect(), as you do from the client side.
                 */
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                bluetoothStuff.handleEstablishedConnection(socket);
                // we want more than 1 player so we don't close the socket and break here, we just try again!
            }
        }
        bluetoothStuff.handleEndedConnection();
    }


    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(BluetoothStuff.TAG, "Could not close the connect socket", e);
        }
        bluetoothStuff.handleEndedConnection();
    }

}
