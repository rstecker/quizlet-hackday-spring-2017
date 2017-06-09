package sixarmstudios.quizletcolors.connections;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.example.bluetooth.server.HostService;
import com.example.bluetooth.server.IServerService;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;

/**
 * Created by rebeccastecker on 6/9/17.
 */

public class HostServiceConnection implements ServiceConnection {
    public static final String TAG = HostServiceConnection.class.getSimpleName();

    private boolean mServerBound = false;
    IServerService mServerService;
    int msgCount = 0;

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        // We've bound to LocalService, cast the IBinder and get LocalService instance
        HostService.LocalBinder binder = (HostService.LocalBinder) service;
        mServerService = binder.getService();
        mServerService.getMessageUpdates()
                .take(1)
                .flatMap((s) -> Flowable.interval(2, TimeUnit.SECONDS))
                .subscribe(
                        (t) -> {
                            mServerService.sendMsg("Tick tock, I've seen " + msgCount);
                        },
                        (e) -> Log.e(TAG, "Ping back error " + e));

        mServerService.getMessageUpdates()
                .scan(
                        (s1, s2) -> {
                            ++msgCount;
                            String newString = "[" + msgCount + "] " + s2 + "\n" + s1;
                            return newString.substring(0, Math.min(300, newString.length()));
                        })
                .subscribe(
                        (text) -> {
                            mDebugHost.setText(text);
                        },
                        (e) -> {
                            Log.e(TAG, "Received error from server service : " + e);
                        },
                        () -> {
                            mDebugHost.setText("Connection shut down : " + msgCount);
                        }
                );
        mServerBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        mServerBound = false;
    }
};
