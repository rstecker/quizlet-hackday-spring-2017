package com.example.bluetooth.server;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.bluetooth.core.BluetoothHostStuff;
import com.example.bluetooth.core.CommsHandler;
import com.example.bluetooth.core.IBluetoothHostListener;

import java.io.IOException;

import io.reactivex.Flowable;
import io.reactivex.processors.BehaviorProcessor;

import static com.example.bluetooth.core.BluetoothTalker.INIT_MSG;

/**
 * Created by rebeccastecker on 6/7/17.
 */

public class HostService extends Service implements IServerService {
    public static final String TAG = HostService.class.getSimpleName();

    private BluetoothHostStuff mBluetoothStuff;
    private final IBinder mBinder = new LocalBinder();
    private final BehaviorProcessor<String> mIncomingMsgs = BehaviorProcessor.create();

    public static Intent createStartIntent(Context context) {
        return new Intent(context, HostService.class);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public IServerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return HostService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, Thread.currentThread().getName() + "] I see us trying to start w/ client id (flags [" + flags + "], startId [" + startId + "]) : " + intent);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, Thread.currentThread().getName() + "] I see an onBind request : " + intent);
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, Thread.currentThread().getName() + "] I see an onUNbind request : " + intent);
        // TODO : start countdown timer to shut down service
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, Thread.currentThread().getName() + "] I see an onREbind request : " + intent);
        // TODO : stop countdown timer, keep service
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, TAG + " done", Toast.LENGTH_SHORT).show();
        if (mBluetoothStuff != null) {
            mBluetoothStuff.shutDown(this);
        }
    }


    private Handler getHandler() {
        return new CommsHandler(mIncomingMsgs::onNext, () -> mIncomingMsgs.onNext(""));//mIncomingMsgs::onComplete); mIncomingMsgs::onComplete);
    }
    //region interface methods

    @Override
    public @Nullable String startHosting(IBluetoothHostListener listener) {
        if (mBluetoothStuff == null) {
            mBluetoothStuff = new BluetoothHostStuff(listener, getHandler());
        }
        if (!mBluetoothStuff.ensureBluetoothSetup(this)) {
            return null;
        }
        mBluetoothStuff.beVisibleToDevices(listener);
        try {
            return mBluetoothStuff.startAsServer(this);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error encountered : " + e);
        }
        return null;
    }

    @Override
    public int connectionCount() {
        return mBluetoothStuff == null ? 0 : mBluetoothStuff.getConnectionCount();
    }

    @Override
    public void sendMsg(@NonNull String msg) {
        if (mBluetoothStuff != null) {
            mBluetoothStuff.broadcastMessage(msg);
        }
    }

    @Override
    public @NonNull Flowable<String> getMessageUpdates() {
        return mIncomingMsgs
                .filter((msg) -> {
                    if (msg.equals(INIT_MSG)) {
                        Log.i(TAG, "Init message received, connection established");
                        return false;
                    }
                    return true;
                });
    }
    //endregion
}

