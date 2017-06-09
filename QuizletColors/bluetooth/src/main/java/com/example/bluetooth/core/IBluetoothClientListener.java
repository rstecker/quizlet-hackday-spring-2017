package com.example.bluetooth.core;

import android.support.annotation.NonNull;

/**
 * Created by rebeccastecker on 6/7/17.
 */

public interface IBluetoothClientListener {
    void requestPermission(@NonNull String[] requestedPermissions);

    /**
     * All this stuff requires Bluetooth! This is called when we detect that the device does
     * not have Bluetooth turned on
     */
    void requestBluetooth();
}
