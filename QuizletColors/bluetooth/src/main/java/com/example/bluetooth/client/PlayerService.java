package com.example.bluetooth.client;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.bluetooth.core.BluetoothPlayerStuff;
import com.example.bluetooth.core.CommsHandler;
import com.example.bluetooth.core.IBluetoothPlayerListener;

import io.reactivex.Flowable;
import io.reactivex.processors.BehaviorProcessor;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

import static com.example.bluetooth.core.BluetoothTalker.INIT_MSG;

/**
 * Created by rebeccastecker on 6/7/17.
 */

public class PlayerService extends Service implements IClientService {
    public static final String TAG = PlayerService.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder();

    BluetoothPlayerStuff mBluetoothStuff;
    Subject<String> mOutgoingMsgs = BehaviorSubject.create();
    BehaviorProcessor<String> mIncomingMsgs = BehaviorProcessor.create();

    public static Intent createStartIntent(Context context) {
        Intent intent = new Intent(context, PlayerService.class);
        return intent;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public IClientService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PlayerService.this;
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
        return new CommsHandler(mIncomingMsgs::onNext, () -> mIncomingMsgs.onNext(""));//mIncomingMsgs::onComplete);
    }
    //region IClientService interface methods

    @Override
    public void startLooking(IBluetoothPlayerListener listener) {
        if (mBluetoothStuff == null) {
            mBluetoothStuff = new BluetoothPlayerStuff(listener, getHandler());
        }
        if (!mBluetoothStuff.ensureBluetoothSetup(this)) {
            return;
        }
        mBluetoothStuff.lookForDevices(this);
    }

    @Override
    public void connectToServer(BluetoothDevice device) {
        Log.i(TAG, "Totes going to do something with this now... " + device.getAddress());
        mBluetoothStuff.connectToHostDevice(this, device);
    }

    @Override
    public void sendMsg(String msg) {
        mOutgoingMsgs.onNext(msg);
    }

    @Override
    public boolean isConnected() {
        return mBluetoothStuff != null && mBluetoothStuff.hasActiveConnection();
    }

    @Override
    public Flowable<String> getMessageUpdates() {
        return mIncomingMsgs
                .distinctUntilChanged()
                .filter((msg) -> {
                    if (msg.equals(INIT_MSG)) {
                        Log.i(TAG, "Init message received, connection established");
                        mBluetoothStuff.pipeThroughIncomingMessages(mOutgoingMsgs);
                        return false;
                    }
                    return true;
                })
                ;
    }
    //endregion
}
