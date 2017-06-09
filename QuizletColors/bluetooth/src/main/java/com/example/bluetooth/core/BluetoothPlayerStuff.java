package com.example.bluetooth.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by rebeccastecker on 6/8/17.
 */

public class BluetoothPlayerStuff extends BluetoothStuff {

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.i(TAG, "Device found : " + deviceName + " [" + deviceHardwareAddress + "]");
                mPlayerListener.onDeviceFound(device, device.getName(), device.getBondState(), device.getAddress());
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                mPlayerListener.isDiscoverable(false);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                mPlayerListener.isDiscoverable(true);
            } else {
                Log.i(TAG, "... what is this?? " + action);
            }
        }
    };

    /**
     * Used to track the state of : context.registerReceiver(mReceiver, bluetoothFilter);
     */
    private boolean mIsRegistered = false;
    private final IBluetoothPlayerListener mPlayerListener;
    private BluetoothTalker mTalker;

    public BluetoothPlayerStuff(IBluetoothPlayerListener listener, Handler handler) {
        super(listener, handler);
        mPlayerListener = listener;
    }

    @Override
    public void shutDown(Context context) {
        super.shutDown(context);
        // Don't forget to unregister the ACTION_FOUND receiver.
        if (mIsRegistered) {
            context.unregisterReceiver(mReceiver);
        }
    }

    /**
     * Kicks off a {@link PlayerThread} to manage the connection to an external targeted device
     */
    public void connectToHostDevice(Context context, BluetoothDevice device) {
        new PlayerThread(this, device).start();
        // Cancel discovery because it otherwise slows down the connection.
        shutDownDiscovery();
    }

    public void lookForDevices(Context context) {
        IntentFilter bluetoothFilter = new IntentFilter();
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        bluetoothFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        context.registerReceiver(mReceiver, bluetoothFilter);
        mIsRegistered = true;

        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        Log.i(TAG, "Just kicked off startDiscovery: " + mBluetoothAdapter.startDiscovery());
    }

    @Override
    void handleEstablishedConnection(BluetoothSocket socket) {
        mTalker = new BluetoothTalker(mHandler, socket);
        mTalker.start();
        mTalker.write("sharks!");
    }

    private void shutDownDiscovery() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    public void sendMessage(@NonNull String msg) {
        mTalker.write(msg);
    }

    public boolean hasActiveConnection() {
        return mTalker != null;
    }
}
