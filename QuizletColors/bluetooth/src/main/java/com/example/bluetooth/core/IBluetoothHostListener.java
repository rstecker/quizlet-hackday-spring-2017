package com.example.bluetooth.core;

import android.content.Intent;
import android.support.annotation.NonNull;

/**
 * Created by rebeccastecker on 6/8/17.
 */

public interface IBluetoothHostListener extends IBluetoothClientListener {
    void requestDiscoverabilityIntent(@NonNull Intent intent);
}
