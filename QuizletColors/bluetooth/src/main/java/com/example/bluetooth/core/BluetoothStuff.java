package com.example.bluetooth.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.bluetooth.client.PlayerService;
import com.example.bluetooth.server.HostService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Backs both the {@link HostService} and the {@link PlayerService}
 * on the lower Bluetooth level
 */
public abstract class BluetoothStuff {
    public static final UUID APP_UUID = UUID.fromString("c6293521-3154-4765-933b-19219e20f0a9");
    public static final String TAG = BluetoothStuff.class.getSimpleName();

    private final IBluetoothClientListener mClientListener;
    Handler mHandler;
    BluetoothAdapter mBluetoothAdapter;


    public BluetoothStuff(IBluetoothClientListener listener, Handler handler) {
        mClientListener = listener;
        mHandler = handler;
    }

    /**
     * Used for initial health check before we begin setting up the connection
     *
     * @return true if we are good to go! False if we have to ask for things.
     * The {@link IBluetoothClientListener} should receive updates regarding all blockers encountered
     */
    public boolean ensureBluetoothSetup(Context context) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(context, "Yo, dawg, no blue tooth. So sad!", Toast.LENGTH_LONG).show();
            throw new IllegalStateException("sucks");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth not enabled, no go!");
            mClientListener.requestBluetooth();
            return false;
        }
        if (!verifyPermission(context, mClientListener)) {
            Log.e(TAG, "Missing permissions, no go!");
            return false;
        }
        return true;
    }

    public void shutDown(Context context) {
    }

    /**
     * @return true if the app has all the permissions it needs. Otherwise it'll be hassling the
     * listener via {@link IBluetoothClientListener#requestPermission(String[])}
     */
    private boolean verifyPermission(Context context, IBluetoothClientListener listener) {
        int accessCoarseLocation = context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        List<String> listRequestPermission = new ArrayList<String>();
        if (accessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listRequestPermission.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (!listRequestPermission.isEmpty()) {
            String[] strRequestPermission = listRequestPermission.toArray(new String[listRequestPermission.size()]);
            listener.requestPermission(strRequestPermission);
        }
        return listRequestPermission.isEmpty();
    }


    void handleEndedConnection() {
        Log.e(TAG, "Primary connection has ended");
    }

    abstract void handleEstablishedConnection(BluetoothSocket socket);
}
