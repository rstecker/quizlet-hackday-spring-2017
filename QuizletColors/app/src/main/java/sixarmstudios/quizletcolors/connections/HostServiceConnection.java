package sixarmstudios.quizletcolors.connections;

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

import javax.annotation.ParametersAreNonnullByDefault;

import gamelogic.BoardState;
import gamelogic.EndState;
import gamelogic.LobbyState;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import sixarmstudios.quizletcolors.logic.engine.GameEngine;
import sixarmstudios.quizletcolors.logic.engine.IGameEngine;
import sixarmstudios.quizletcolors.logic.player.IPlayerEngine;
import sixarmstudios.quizletcolors.logic.player.PlayerEngine;
import ui.Fact;

/**
 * Created by rebeccastecker on 6/9/17.
 */
@ParametersAreNonnullByDefault
public class HostServiceConnection implements ServiceConnection {
    public static final String TAG = HostServiceConnection.class.getSimpleName();

    private boolean mServerBound = false;
    private IServerService mServerService;
    private IGameEngine mGameEngine;
    private IPlayerEngine mPlayerEngine;
    private BehaviorSubject<Boolean> mBoundSubject = BehaviorSubject.create();
    private int msgCount = 0;
    private ObjectMapper mMapper;

    public HostServiceConnection() {
        Log.i(TAG, "Starting the game engine and host service");
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
                            e.printStackTrace(System.out);
                            Log.e(TAG, "Incoming msg : " + e);
                            throw new IllegalStateException("Remote Player engine died: "+e);
                        })

        ;

        // This is us listening to our local player
        mPlayerEngine.getOutgoingBluetoothMessages().subscribe(
                (msg) -> {
                    sendOutResponse(mGameEngine.processMessage(msg));   // we want to wait till we're bound before we try and send things
                },
                (e) -> {
                    e.printStackTrace(System.out);
                    Log.e(TAG, "Error handling outgoing Player msg : " + e);
                    throw new IllegalStateException("Local Player engine died: "+e);
                }
        );
        mServerBound = true;
        mBoundSubject.onNext(true);
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        mServerBound = false;
        mBoundSubject.onNext(false);
    }

    public String startHosting(IBluetoothHostListener listener, String username) {
        // FIXME : I should PROBABLY check to make sure this is bound/wait for it... being lazy atm
        mPlayerEngine.initializePlayer(username);
        return mServerService.startHosting(listener);
    }

    @Override public void onBindingDied(ComponentName name) {
        Log.i(TAG, "Binding died : "+name);
    }

    public void unbindService(Context context) {
        Log.i(TAG, "Service ounbound "+context);
        context.unbindService(this);
        mServerBound = false;
        mBoundSubject.onNext(false);
    }

    public boolean isBound() {
        return mServerBound;
    }

    public void makeMove(@NonNull String answer, @NonNull String color) {
        mBoundSubject
                .filter((bound) -> bound)
                .take(1)
                .subscribe((bound) -> mPlayerEngine.makeMove(answer, color), (e) -> Log.e(TAG, "Error "+e));
    }


    public Observable<EndState> getEndStateUpdates() {
        return mPlayerEngine.getEndStateUpdates().observeOn(AndroidSchedulers.mainThread());
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

    public void setContent(@NonNull String setName, @Nullable List<Fact> facts) {
        if (facts == null) {
            mGameEngine.setContent("", new ArrayList<>());
            return;
        }
        List<Pair<String, String>> content = new ArrayList<>();
        for (Fact fact : facts) {
            content.add(new Pair<>(fact.question, fact.answer));
        }
        // TODO : are we overriding? are we adding? where are we screening for conflicting values?
        mGameEngine.setContent(setName, content);
    }

    public void startGame(QCGameMessage.GameType gameType, Integer gameTarget) {
        Completable.defer(
                () -> {
                    sendOutResponse(mGameEngine.startGame(gameType, gameTarget));
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe();
    }
};
