package sixarmstudios.quizletcolors.connections;

import android.arch.lifecycle.LifecycleActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import com.example.bluetooth.core.IBluetoothHostListener;
import com.example.bluetooth.server.HostService;
import com.example.bluetooth.server.IServerService;
import com.example.myapplication.bluetooth.QCGameMessage;
import com.example.myapplication.bluetooth.QCPlayerMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import sixarmstudios.quizletcolors.logic.engine.GameEngine;
import sixarmstudios.quizletcolors.logic.engine.IGameEngine;
import sixarmstudios.quizletcolors.logic.player.IPlayerEngine;
import sixarmstudios.quizletcolors.logic.player.PlayerEngine;
import ui.BoardState;
import ui.Fact;
import ui.LobbyState;

/**
 * Created by rebeccastecker on 6/9/17.
 */

public class HostServiceConnection implements ServiceConnection {
    public static final String TAG = HostServiceConnection.class.getSimpleName();

    private boolean mServerBound = false;
    private IServerService mServerService;
    private IGameEngine mGameEngine;
    private IPlayerEngine mPlayerEngine;
    private int msgCount = 0;
    private ObjectMapper mMapper;

    public HostServiceConnection(LifecycleActivity context) {
        mGameEngine = new GameEngine();
        mPlayerEngine = new PlayerEngine();
        mMapper = new ObjectMapper();
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        // We've bound to LocalService, cast the IBinder and get LocalService instance
        HostService.LocalBinder binder = (HostService.LocalBinder) service;
        mServerService = binder.getService();
        // This is us listening to remote players
        mServerService.getMessageUpdates()
                .filter(StringUtils::isNoneEmpty)
                .map((s) -> mMapper.readValue(s, QCPlayerMessage.class))
                .subscribe(
                        (msg) -> {
                            sendOutResponse(mGameEngine.processMessage(msg));
                        },
                        (e) -> {
                            Log.e(TAG, "Incoming msg : " + e);
                        })

        ;

        // This is a test system that just posts a slow loop welcome msg
        Observable.interval(1, 15, TimeUnit.SECONDS)
                .map(
                        (t) -> {
                            return mGameEngine.generateBaseLobbyMessage(QCGameMessage.Action.LOBBY_WELCOME);
                        })
                .subscribe(
                        this::sendOutResponse,
                        (e) -> {
                            Log.e(TAG, "Error sending msg : " + e);
                        })
        ;

        // This is us listening to our local player
        mPlayerEngine.getOutgoingBluetoothMessages().subscribe(
                (msg) -> {
                    sendOutResponse(mGameEngine.processMessage(msg));   // we want to wait till we're bound before we try and send things
                },
                (e) -> {
                    Log.e(TAG, "Error handling outgoing Player msg : " + e);
                }
        );
        mServerBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        mServerBound = false;
    }

    public String startHosting(IBluetoothHostListener listener, String username) {
        mPlayerEngine.initializePlayer(username);
        return mServerService.startHosting(listener);
    }

    public void unbindService(Context context) {
        context.unbindService(this);
        mServerBound = false;
    }

    public boolean isBound() {
        return mServerBound;
    }

    public void makeMove(@NonNull String answer, @NonNull String color) {
        if (isBound()) {
            mPlayerEngine.makeMove(answer, color);
        }
    }

    public Observable<BoardState> getBoardStateUpdates() {
        return mPlayerEngine.getBoardStateUpdates().observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<LobbyState> getLobbyStateUpdates() {
        return mPlayerEngine.getLobbyStateUpdates().observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Boolean> getStartStatusUpdates() {
        return mGameEngine.isAbleToStart().observeOn(AndroidSchedulers.mainThread());
    }

    private void sendOutResponse(QCGameMessage gameMessage) throws JsonProcessingException {
        if (isBound()) {
            String msg = mMapper.writeValueAsString(gameMessage);
            Log.v(TAG, "Attempting to send host msg : " + msg);
            mServerService.sendMsg(msg);
            mPlayerEngine.processMessage(gameMessage);
        }
    }

    public void setContent(@Nullable List<Fact> facts) {
        if (facts == null) {
            mGameEngine.setContent(new ArrayList<>());
            return;
        }
        List<Pair<String, String>> content = new ArrayList<>();
        for(Fact fact : facts) {
            content.add(new Pair<>(fact.question, fact.answer));
        }
        // TODO : are we overriding? are we adding? where are we screening for conflicting values?
        mGameEngine.setContent(content);
    }
};
