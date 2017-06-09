package com.example.bluetooth.core;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by rebeccastecker on 6/8/17.
 */

public interface IBluetoothPlayerListener extends IBluetoothClientListener {
    /**
     * When a user asks to join a game, they can find devices to pair with. Every time a device
     * is found, it is bubbled up via this method. There is no one absolute list of found devices
     * available though, so keep track of this!
     * <p>
     * Starts being called after {@link com.example.bluetooth.client.IClientService#startLooking(IBluetoothClientListener)}
     * is called.
     *
     * @param bondState see: {@link BluetoothDevice#BOND_NONE},
     *                  {@link BluetoothDevice#BOND_BONDING},
     *                  {@link BluetoothDevice#BOND_BONDED}
     */
    void onDeviceFound(@NonNull BluetoothDevice device, @Nullable String name, int bondState, @NonNull String address);


    /**
     * Update about discoverability state. Can be triggered when a user attempts to join a game.
     */
    void isDiscoverable(boolean isDiscoverable);
}
