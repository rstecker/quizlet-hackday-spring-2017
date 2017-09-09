package sixarmstudios.quizletcolors.connections;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.bluetooth.client.IClientService;
import com.example.bluetooth.client.PlayerService;
import com.example.bluetooth.core.IBluetoothPlayerListener;
import com.example.myapplication.bluetooth.QCGameMessage;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.ParametersAreNonnullByDefault;

import gamelogic.BoardState;
import gamelogic.LobbyState;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.BehaviorSubject;
import sixarmstudios.quizletcolors.logic.player.IPlayerEngine;
import sixarmstudios.quizletcolors.logic.player.PlayerEngine;

/**
 * Created by rebeccastecker on 6/10/17.
 */
@ParametersAreNonnullByDefault
public class PlayerServiceConnection implements ServiceConnection {
    public static final String TAG = PlayerServiceConnection.class.getSimpleName();
    private IClientService mClientService;
    private boolean mClientBound = false;
    private BehaviorSubject<Boolean> mBoundSubject = BehaviorSubject.create();
    private int msgCount = 0;
    private ObjectMapper mMapper;
    private IPlayerEngine mEngine;

    public PlayerServiceConnection() {
        mEngine = new PlayerEngine();
        mMapper = new ObjectMapper();
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        // We've bound to LocalService, cast the IBinder and get LocalService instance
        PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
        mClientService = binder.getService();
        mClientService.getMessageUpdates()
                .map(
                        (s) -> {
                            return mMapper.readValue(s, QCGameMessage.class);
                        })
                .subscribe(
                        (msg) -> {
                            mEngine.processMessage(msg);
                        },
                        (e) -> {
                            Log.e(TAG, "Error w/ incoming msg : " + e);
                        })
        ;
        // this is us piping our actions to the server. We want to wait till we establish a connection before we go
        mEngine.getOutgoingBluetoothMessages().subscribe(
                (msg) -> {
                    if (isBound()) {
                        String stringMsg = mMapper.writeValueAsString(msg);
                        mClientService.sendMsg(stringMsg);
                        Log.v(TAG, "Attempting to send client msg : " + stringMsg);
                    }
                },
                (e) -> {
                    Log.e(TAG, "Error posting player message");
                }
        );
        mClientBound = true;
        mBoundSubject.onNext(true);
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        mClientBound = false;
    }

    public void unbindService(Context context) {
        context.unbindService(this);
        mClientBound = false;
        mBoundSubject.onNext(false);
    }

    public boolean isBound() {
        return mClientBound;
    }

    public void startLooking(IBluetoothPlayerListener listener) {
        mBoundSubject.filter((bound) -> bound)
                .take(1)
                .subscribe((bound) -> {
                    mClientService.startLooking(listener);
                });
    }

    public void connectToServer(BluetoothDevice device, String username) {
        mBoundSubject.filter((bound) -> bound)
                .take(1)
                .subscribe((bound) -> {
                    mClientService.connectToServer(device);
                    mEngine.initializePlayer(username);
                });
    }

    public void makeMove(@NonNull String answer, @NonNull String color) {
        if (isBound()) {
            mEngine.makeMove(answer, color);
        }
    }

    public Observable<BoardState> getBoardStateUpdates() {
        return mEngine.getBoardStateUpdates().observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<LobbyState> getLobbyStateUpdates() {
        return mEngine.getLobbyStateUpdates().observeOn(AndroidSchedulers.mainThread());
    }
}
