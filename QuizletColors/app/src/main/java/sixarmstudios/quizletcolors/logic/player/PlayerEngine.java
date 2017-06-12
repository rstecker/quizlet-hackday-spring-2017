package sixarmstudios.quizletcolors.logic.player;

import android.support.annotation.NonNull;

import com.example.myapplication.bluetooth.GameState;
import com.example.myapplication.bluetooth.ImmutableQCMove;
import com.example.myapplication.bluetooth.QCGameMessage;
import com.example.myapplication.bluetooth.QCMember;
import com.example.myapplication.bluetooth.QCPlayerMessage;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import gamelogic.BoardState;
import gamelogic.LobbyState;
import ui.Player;

public class PlayerEngine implements IPlayerEngine {
    private final Subject<BoardState> mBoardState = BehaviorSubject.create();
    private final Subject<LobbyState> mLobbyState = BehaviorSubject.create();
    private final Subject<QCPlayerMessage> mOutgoingMsgs = BehaviorSubject.create();
    private String mUsername;

    @Override public void initializePlayer(@NonNull String username) {
        mUsername = username;
        mOutgoingMsgs.onNext(QCPlayerMessage.build(QCPlayerMessage.Action.JOIN_GAME, GameState.LOBBY, username));
    }

    @Override public void processMessage(@NonNull QCGameMessage message) {
        switch (message.state()) {
            case PLAYING:
                handleGameUpdate(message);
                break;
            case LOBBY:
                handleLobbyUpdate(message);
                break;
            default:
                throw new UnsupportedOperationException("Unable to handle engine state : " + message.state());
        }
    }

    @Override public void makeMove(@NonNull String answer, @NonNull String color) {
        mOutgoingMsgs.onNext(
                QCPlayerMessage.build(
                        QCPlayerMessage.Action.PLAYER_MOVE,
                        GameState.PLAYING,
                        mUsername,
                        ImmutableQCMove.builder().answer(answer).color(color).build()
                ));
    }

    @Override public Observable<BoardState> getBoardStateUpdates() {
        return mBoardState;
    }

    @Override public Observable<LobbyState> getLobbyStateUpdates() {
        return mLobbyState;
    }

    @Override public Observable<QCPlayerMessage> getOutgoingBluetoothMessages() {
        return mOutgoingMsgs;
    }

    private void handleLobbyUpdate(QCGameMessage message) {
        switch (message.action()) {
            case GAME_UPDATE:
            case OUT_OF_SYNC:
            case LOBBY_UPDATE:
            case LOBBY_WELCOME:
            case ACCEPT_REQUEST:
            case REJECT_REQUEST:
            case INVALID_STATE:
                mLobbyState.onNext(extractLobbyState(message));
                break;

            case LOBBY_CANCEL:
            case LOBBY_KICK:
                throw new UnsupportedOperationException("I totally should handle this action (" + message.action() + ") but we have no game-restart/re-join logic yet");

            case CORRECT_ANSWER:
            case BAD_ANSWER:
            case WRONG_USER:
            default:
                throw new UnsupportedOperationException("Unable to handle lobby action : " + message.action());

        }
    }


    private void handleGameUpdate(QCGameMessage message) {
        switch (message.action()) {
            case GAME_UPDATE:
            case INVALID_STATE:
            case OUT_OF_SYNC:
                mBoardState.onNext(extractBoardState(message));
                break;

            case BAD_ANSWER:
            case WRONG_USER:
            case CORRECT_ANSWER:
                // TODO : these should have extra UI associated with them. They may or may not change
                //      OUR board state but it does indicate something is going on...
                mBoardState.onNext(extractBoardState(message));
                break;

            case LOBBY_WELCOME:
            case ACCEPT_REQUEST:
            case REJECT_REQUEST:
            case LOBBY_UPDATE:
            case LOBBY_CANCEL:
            case LOBBY_KICK:
            default:
                throw new UnsupportedOperationException("Unable to handle game action : " + message.action());

        }
    }

    private LobbyState extractLobbyState(QCGameMessage message) {
        List<Player> players = new ArrayList<>();
        for (QCMember member : message.members()) {
            players.add(new Player(
                    member.username(),
                    member.color() == null ? "#000000" : member.color(),
                    member.isHost(),
                    mUsername.equals(member.username())
            ));
        }
        return LobbyState.build(players);
    }

    private BoardState extractBoardState(QCGameMessage message) {
        List<Player> players = new ArrayList<>();
        String question = null;
        List<String> options = null;
        for (QCMember member : message.members()) {
            players.add(new Player(
                    member.username(),
                    member.color(),
                    member.isHost(),
                    mUsername.equals(member.username())
            ));
            if (mUsername.equals(member.username())) {
                question = member.question();
                options = member.options();
            }
        }
        return BoardState.build(question, options, players, null, null);
    }
}
