package sixarmstudios.quizletcolors.logic.player;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.myapplication.bluetooth.GameState;
import com.example.myapplication.bluetooth.ImmutableQCMove;
import com.example.myapplication.bluetooth.QCGameMessage;
import com.example.myapplication.bluetooth.QCMember;
import com.example.myapplication.bluetooth.QCPlayerMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gamelogic.BoardState;
import gamelogic.EndState;
import gamelogic.ImmutableEndState;
import gamelogic.LobbyState;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import ui.BadMove;
import ui.GoodMove;
import ui.Player;

public class PlayerEngine implements IPlayerEngine {
    public static final String TAG = PlayerEngine.class.getSimpleName();
    private final Subject<EndState> mEndState = BehaviorSubject.create();
    private final Subject<BoardState> mBoardState = BehaviorSubject.create();
    private final Subject<LobbyState> mLobbyState = BehaviorSubject.create();
    private final Subject<QCPlayerMessage> mOutgoingMsgs = BehaviorSubject.create();
    private String mUsername;
    private int mMessageCount;

    @Override
    public void initializePlayer(@NonNull String username) {
        mUsername = username;
        mOutgoingMsgs.onNext(QCPlayerMessage.build(mMessageCount, QCPlayerMessage.Action.JOIN_GAME, GameState.LOBBY, username));
    }

    @Override
    public synchronized void processMessage(@NonNull QCGameMessage message) {
        if ((mMessageCount + 1) != message.msgCount()) {
            Log.w(TAG, "There appears to be skew in our msg handling. Warning. Current count " + mMessageCount + " vs game update " + message.msgCount() + " :: " + message.state());
        }
        mMessageCount = message.msgCount();
        synchronized (TAG) {
            switch (message.state()) {
                case PLAYING:
                    handleGameUpdate(message);
                    break;
                case LOBBY:
                    handleLobbyUpdate(message);
                    break;
                case ENDED:
                    handleEndUpdate(message);
                    break;
                default:
                    throw new UnsupportedOperationException("Unable to handle engine state : " + message.state());
            }
        }
    }

    @Override
    public void makeMove(@NonNull String answer, @NonNull String color) {
        mOutgoingMsgs.onNext(
                QCPlayerMessage.build(
                        mMessageCount,
                        QCPlayerMessage.Action.PLAYER_MOVE,
                        GameState.PLAYING,
                        mUsername,
                        ImmutableQCMove.builder().answer(answer).color(color).build()
                ));
    }

    @Override
    public Observable<BoardState> getBoardStateUpdates() {
        return mBoardState;
    }

    @Override
    public Observable<LobbyState> getLobbyStateUpdates() {
        return mLobbyState;
    }

    @Override
    public Observable<EndState> getEndStateUpdates() {
        return mEndState;
    }

    @Override
    public Observable<QCPlayerMessage> getOutgoingBluetoothMessages() {
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


    private void handleEndUpdate(QCGameMessage message) {
        mEndState.onNext(extractEndGameBoardState(message));
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
                    mUsername.equals(member.username()),
                    member.score() == null ? 0 : member.score()
            ));
        }
        return LobbyState.build(message.setName(), message.factCount(), players);
    }

    private BoardState extractBoardState(QCGameMessage message) {
        List<Player> players = new ArrayList<>();
        QCMember currentPlayer = null;
        String question = null;
        List<String> options = null;
        for (QCMember member : message.members()) {
            boolean isCurrentPlayer = mUsername.equals(member.username());
            players.add(new Player(
                    member.username(),
                    member.color(),
                    member.isHost(),
                    isCurrentPlayer,
                    member.score() == null ? 0 : member.score()
            ));
            if (isCurrentPlayer) {
                question = member.question();
                options = member.options();
                currentPlayer = member;
            }
        }
        // TODO : handle player missing. Either we lost our username or we got kicked
        return BoardState.build(question, options, players,
                message.gameType(), message.gameTarget(),
                extractPossibleGoodMove(message, currentPlayer),
                extractPossibleBadMove(message, currentPlayer)
        );
    }

    private EndState extractEndGameBoardState(QCGameMessage message) {
        List<Player> players = new ArrayList<>();
        QCMember currentPlayer = null;
        for (QCMember member : message.members()) {
            boolean isCurrentPlayer = mUsername.equals(member.username());
            players.add(new Player(
                    member.username(),
                    member.color(),
                    member.isHost(),
                    isCurrentPlayer,
                    member.score() == null ? 0 : member.score()
            ));
        }
        // TODO : handle player missing. Either we lost our username or we got kicked
        return ImmutableEndState.builder()
                .players(players)
                .gameTarget(message.gameTarget())
                .gameType(message.gameType())
                .build();
    }

    private BadMove extractPossibleBadMove(@NonNull QCGameMessage message, @NonNull QCMember currentPlayer) {
        if (message.action() != QCGameMessage.Action.BAD_ANSWER
                && message.action() != QCGameMessage.Action.WRONG_USER) {
            return null;
        }
        BadMove bm = new BadMove();
        bm.timestamp = new Date().getTime();
        bm.offeredAnswer = message.providedAnswer();
        bm.offeredAnswerColor = message.answererColor();
        bm.incorrectQuestion = message.answeredQuestion();
        bm.incorrectQuestionColor = message.providedColor();
        bm.correctQuestion = message.question();
        bm.correctQuestionColor = message.questionColor();
        bm.correctAnswer = message.correctAnswer();
        bm.correctAnswerColor = message.correctAnswerColor();
        bm.youWereGivenBadAnswer = currentPlayer.reaction() == QCMember.Reaction.RECEIVED_BAD_ANSWER;
        bm.youAnsweredPoorly = currentPlayer.reaction() == QCMember.Reaction.WRONG_CHOICE || currentPlayer.reaction() == QCMember.Reaction.WRONG_USER;
        bm.youFailedToAnswer = currentPlayer.reaction() == QCMember.Reaction.FAILED_TO_ANSWER;
        bm.yourAnswerWentToSomeoneElse = currentPlayer.reaction() == QCMember.Reaction.FAILED_TO_RECEIVE_ANSWER;
        return bm;
    }

    private GoodMove extractPossibleGoodMove(@NonNull QCGameMessage message, @NonNull QCMember currentPlayer) {
        if (message.action() != QCGameMessage.Action.CORRECT_ANSWER) {
            return null;
        }
        GoodMove gm = new GoodMove();
        gm.timestamp = new Date().getTime();
        gm.answer = message.providedAnswer();
        gm.question = message.question();
        gm.youAnswered = currentPlayer.reaction() == QCMember.Reaction.CORRECT_ANSWER_PROVIDED;
        gm.youAsked = currentPlayer.reaction() == QCMember.Reaction.CORRECT_ANSWER_RECEIVED;
        gm.askerColor = message.questionColor();
        if (gm.askerColor != null && message.findPlayerByColor(gm.askerColor) != null) {
            gm.askerUsername = message.findPlayerByColor(gm.askerColor).username();
        }
        gm.answererColor = message.providedColor();
        if (gm.answererColor != null && message.findPlayerByColor(gm.answererColor) != null) {
            gm.answererUsername = message.findPlayerByColor(gm.answererColor).username();
        }
        return gm;
    }
}
