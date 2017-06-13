package sixarmstudios.quizletcolors.connections;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.ViewModelProviders;
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

import gamelogic.BoardState;
import gamelogic.LobbyState;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import sixarmstudios.quizletcolors.logic.player.IPlayerEngine;
import sixarmstudios.quizletcolors.logic.player.PlayerEngine;
import viewmodel.TopLevelViewModel;

/**
 * Created by rebeccastecker on 6/10/17.
 */

public class PlayerServiceConnection implements ServiceConnection {
    public static final String TAG = PlayerServiceConnection.class.getSimpleName();
    private IClientService mClientService;
    private boolean mClientBound = false;
    private int msgCount = 0;
    private ObjectMapper mMapper;
    private IPlayerEngine mEngine;

    public PlayerServiceConnection(LifecycleActivity context) {
        mEngine = new PlayerEngine();
        mMapper = new ObjectMapper();

        mEngine.getLobbyStateUpdates().observeOn(Schedulers.newThread()).subscribe(
                (state) -> {
                    TopLevelViewModel model = ViewModelProviders.of(context).get(TopLevelViewModel.class);
                    if (model != null) {
                        Log.i(TAG, "I'm going in : "+Thread.currentThread().getName());
                        model.processLobbyUpdate(state);
                    } else {
                        Log.w(TAG, "I tried to look up the player vm but it was null");
                    }
                },
                (e) -> Log.e(TAG, "Error updating view model ["+Thread.currentThread().getName()+"] " + e)
        );
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
                        Log.v(TAG, "Attempting to send client msg : "+stringMsg);
                    }
                },
                (e) -> {
                    Log.e(TAG, "Error posting player message");
                }
        );
        mClientBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        mClientBound = false;
    }

    public void unbindService(Context context) {
        context.unbindService(this);
        mClientBound = false;
    }

    public boolean isBound() {
        return mClientBound;
    }

    public void startLooking(IBluetoothPlayerListener listener) {
        mClientService.startLooking(listener);
    }

    public void connectToServer(BluetoothDevice device, String username) {
        mClientService.connectToServer(device);
        mEngine.initializePlayer(username);
    }

    public void makeMove(@NonNull String answer, @NonNull String color){
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
