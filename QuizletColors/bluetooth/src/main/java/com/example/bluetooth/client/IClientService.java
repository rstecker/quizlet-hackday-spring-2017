package com.example.bluetooth.client;

import android.bluetooth.BluetoothDevice;

import com.example.bluetooth.core.IBluetoothPlayerListener;

import io.reactivex.Flowable;

/**
 * Created by rebeccastecker on 6/7/17.
 */

public interface IClientService {
    void startLooking(IBluetoothPlayerListener listener);

    void connectToServer(BluetoothDevice device);

    void sendMsg(String msg);

    /**
     * @return true if it's in a state that can handle sending msgs to the other person
     */
    boolean isConnected();

    Flowable<String> getMessageUpdates();
}
