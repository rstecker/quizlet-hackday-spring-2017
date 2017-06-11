package com.example.bluetooth.core;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

/**
 * Used by the non-Host devices to establish communications with a targeted device
 */
class PlayerThread extends Thread {
    public static final String TAG = PlayerThread.class.getSimpleName();
    private BluetoothStuff bluetoothStuff;
    private final BluetoothSocket mSocket;

    public PlayerThread(BluetoothStuff bluetoothStuff, BluetoothDevice device) {
        this.bluetoothStuff = bluetoothStuff;
        BluetoothSocket tmp = null;
        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(BluetoothStuff.APP_UUID);
        } catch (IOException e) {
            Log.e(BluetoothStuff.TAG, "Socket's create() method failed", e);
        }
        mSocket = tmp;
    }

    public void run() {
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            Log.v(TAG, "Waiting for socket to connect");
            mSocket.connect();
            Log.v(TAG, "Socket connected");
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            bluetoothStuff.handleEndedConnection();
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        bluetoothStuff.handleEstablishedConnection(mSocket);
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
        bluetoothStuff.handleEndedConnection();
    }
}
