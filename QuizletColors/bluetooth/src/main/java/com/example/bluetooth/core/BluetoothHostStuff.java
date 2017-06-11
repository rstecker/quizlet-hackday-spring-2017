package com.example.bluetooth.core;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.example.bluetooth.core.BluetoothTalker.INIT_MSG;

/**
 * Created by rebeccastecker on 6/8/17.
 */

public class BluetoothHostStuff extends BluetoothStuff {
    private Map<BluetoothSocket, BluetoothTalker> mPlayers = new HashMap<>();

    public BluetoothHostStuff(IBluetoothHostListener listener, Handler handler) {
        super(listener, handler);
    }

    /**
     * Kicks off a request to the top level regarding Discoverability
     */
    public void beVisibleToDevices(IBluetoothHostListener listener) {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        listener.requestDiscoverabilityIntent(discoverableIntent);
    }

    /**
     * Kicks off a {@link HostThread} to manage all incoming connections
     *
     * @return the name (if known) and address of the bluetooth device
     */
    @SuppressLint("HardwareIds")
    public @NonNull String startAsServer(Context context) throws IOException {
        new HostThread(this).start();
        String name = mBluetoothAdapter.getName();
        if (name == null) {
            return "Unknown " + mBluetoothAdapter.getAddress();
        }
        return name + " " + mBluetoothAdapter.getAddress();
    }

    @Override void handleEstablishedConnection(BluetoothSocket socket) {
        BluetoothTalker talker = new BluetoothTalker(mHandler, socket);
        talker.start();
        talker.write(INIT_MSG);
        mPlayers.put(socket, talker);
        Log.v(TAG, "Host connection established. There are now "+getConnectionCount()+" players connected");
    }

    public int getConnectionCount() {
        return mPlayers.size();
    }

    public void broadcastMessage(@NonNull String msg) {
        for (BluetoothTalker talker : mPlayers.values()) {
            talker.write(msg);
        }
    }
}
