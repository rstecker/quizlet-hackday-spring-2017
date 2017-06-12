package sixarmstudios.quizletcolors.logic.player;

import android.support.annotation.NonNull;

import com.example.myapplication.bluetooth.QCGameMessage;
import com.example.myapplication.bluetooth.QCPlayerMessage;

import io.reactivex.Observable;
import gamelogic.BoardState;
import gamelogic.LobbyState;

/**
 * Logic each player runs, consuming Bluetooth input or user actions and spitting out either
 * UI updates or msgs to be sent over the wire
 */
public interface IPlayerEngine {
    public void initializePlayer(@NonNull String username);
    public void processMessage(@NonNull QCGameMessage message);
    public void makeMove(@NonNull String answer, @NonNull String color);
    public Observable<BoardState> getBoardStateUpdates();
    public Observable<LobbyState> getLobbyStateUpdates();
    public Observable<QCPlayerMessage> getOutgoingBluetoothMessages();
}
