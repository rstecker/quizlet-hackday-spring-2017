package com.example.bluetooth.server;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.bluetooth.core.IBluetoothHostListener;

import io.reactivex.Flowable;

/**
 * Created by rebeccastecker on 6/7/17.
 */

public interface IServerService {
    /**
     * Puts the phone in a hosting mode, waiting for other users to connect
     *
     * @return the name (if available) and address. If the service was not started up
     * correctly, null is returned
     */
    @Nullable String startHosting(@NonNull IBluetoothHostListener listener);

    /**
     * Sends a raw string over blue tooth to the other clients
     *
     * @param msg
     */
    void sendMsg(@NonNull String msg);

    /**
     * @return the number of external devices it can communicate with currently
     */
    int connectionCount();

    /**
     * @return flowable that should be subscribed to in order to process incoming data from other
     * clients
     */
    @NonNull Flowable<String> getMessageUpdates();
}
