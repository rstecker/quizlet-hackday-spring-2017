package sixarmstudios.quizletcolors.logic.engine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import com.example.myapplication.bluetooth.GameState;
import com.example.myapplication.bluetooth.ImmutableQCGameMessage;
import com.example.myapplication.bluetooth.QCGameMessage;
import com.example.myapplication.bluetooth.QCMember;
import com.example.myapplication.bluetooth.QCPlayerMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.ParametersAreNonnullByDefault;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by rebeccastecker on 6/8/17.
 */
@ParametersAreNonnullByDefault
public class GameEngine implements IGameEngine {
    public static final String TAG = GameEngine.class.getSimpleName();
    public static final int MIN_CONTENT_SIZE = 10;

    private ILobbyLogic mLobbyLogic;
    private IPlayLogic mPlayLogic;
    private IEndLogic mEndLogic;
    private IDistributionLogic mDistributionLogic;
    private List<QCMember> mMembers;
    private List<Pair<String, String>> mContent;
    private BehaviorSubject<Boolean> mCanStart;
    private String mSetName;
    @Nullable
    private QCGameMessage.GameType mGameType;
    private int mGameTarget;
    @Nullable
    private Date mGameStart;
    private int mMessageCount;

    public GameEngine() {
        mLobbyLogic = new LobbyLogic(this);
        mPlayLogic = new PlayLogic(this);
        mEndLogic = new EndLogic(this);
        mDistributionLogic = new DistributionLogic();
        mMembers = new ArrayList<>();
        mContent = new ArrayList<>();
        mCanStart = BehaviorSubject.createDefault(false);
    }

    @Override
    public Observable<Boolean> isAbleToStart() {
        return mCanStart.distinctUntilChanged();
    }

    @Override
    public boolean isGameInProgress() {
        return mGameStart != null && !isGameHasEnded();
    }

    @Override
    public boolean isGameHasEnded() {
        if (mGameType == null || mGameStart == null) {
            return false;
        }
        Log.i(TAG, "Checking for game end... " + mGameType + " : " + mGameTarget);
        switch (mGameType) {
            case INFINITE:
                return false;
            case TIMED_GAME:
                long gameOver = (mGameStart.getTime() + TimeUnit.MINUTES.toMillis(mGameTarget));
                long now = new Date().getTime();
                boolean gameIsOver = gameOver < now;
                Log.i(TAG, " >> " + gameOver + " < " + now +" = "+gameIsOver);
                return gameIsOver;
            case ALL_PLAYERS_TO_POINTS:
                for (QCMember member : mMembers) {
                    Log.i(TAG, " >> " + member.getIntScore() + " vs " + mGameTarget);
                    if (member.getIntScore() < mGameTarget) {
                        return false;
                    }
                }
                return true;
            case FIRST_PLAYER_TO_POINTS:
                for (QCMember member : mMembers) {
                    Log.i(TAG, " >> " + member.getIntScore() + " vs " + mGameTarget);
                    if (member.getIntScore() >= mGameTarget) {
                        return true;
                    }
                }
                return false;
            default:
                throw new IllegalStateException("Unknown game type :" + mGameType);
        }
    }

    @NonNull
    @Override
    public QCGameMessage generateBaseEndGameMessage(@NonNull QCGameMessage.Action action) {
        return ImmutableQCGameMessage.builder()
                .msgCount(mMessageCount)
                .action(action)
                .state(GameState.ENDED)
                .members(mMembers)
                .setName(mSetName)
                .factCount(mContent == null ? 0 : mContent.size())
                .build();
    }

    @NonNull
    @Override
    public QCGameMessage generateBaseLobbyMessage(@NonNull QCGameMessage.Action action) {
        return ImmutableQCGameMessage.builder()
                .msgCount(mMessageCount)
                .action(action)
                .state(GameState.LOBBY)
                .members(mMembers)
                .setName(mSetName)
                .factCount(mContent == null ? 0 : mContent.size())
                .build();
    }

    @NonNull
    @Override
    public QCGameMessage generateBasePlayMessage(@NonNull QCGameMessage.Action action) {
        return ImmutableQCGameMessage.builder()
                .msgCount(mMessageCount)
                .action(action)
                .state(GameState.PLAYING)
                .members(mMembers)
                .build();
    }

    @Nullable
    @Override
    public QCMember findMemberByUsername(@NonNull String username) {
        for (QCMember member : mMembers) {
            if (username.equals(member.username())) {
                return member;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public QCMember findMemberByColor(@NonNull String color) {
        for (QCMember member : mMembers) {
            if (color.equals(member.color())) {
                return member;
            }
        }
        return null;
    }

    @Override
    public QCGameMessage startGame(@NonNull QCGameMessage.GameType gameType, int gameTarget) {
        if (!canStart()) {
            throw new IllegalStateException("Game is not valid to start");
        }
        mGameTarget = gameTarget;
        mGameType = gameType;
        mGameStart = new Date();
        mMessageCount = 1;
        return mPlayLogic.startGame(gameType, gameTarget);
    }

    @Override
    public synchronized QCGameMessage processMessage(@NonNull QCPlayerMessage message) {
        if (message.msgCount() != mMessageCount) {
            Log.w(TAG, "Current game engine message count is " + mMessageCount + ", received " + message.msgCount() + " from " + message.username()+" : "+message.state());
            switch (message.state()) {
                case LOBBY:
                    return mLobbyLogic.processMessage(message);
                case PLAYING:
                case ENDED:
                    if (isGameHasEnded()) {
                        return mEndLogic.processMessage(message);
                    }
                    return generateBasePlayMessage(QCGameMessage.Action.OUT_OF_SYNC);
                default:
                    throw new UnsupportedOperationException("Out of sync msg: I don't know how to handle : " + message.state());
            }
        }
        ++mMessageCount;
        switch (message.state()) {
            case LOBBY:
                return mLobbyLogic.processMessage(message);
            case PLAYING:
                if (isGameHasEnded()) {
                    return mEndLogic.processMessage(message);
                }
                QCGameMessage outMessage = mPlayLogic.processMessage(message);
                if (isGameHasEnded()) {
                    return mEndLogic.processMessage(message);
                }
                return outMessage;
            case ENDED:
                return mEndLogic.processMessage(message);
            default:
                throw new UnsupportedOperationException("I don't know how to handle : " + message.state());
        }
    }

    @Override
    public void addMember(@NonNull QCMember newMember) {
        if (mMembers.contains(newMember)) {
            return;
        }
        mMembers.add(newMember);
        mCanStart.onNext(canStart());
    }

    @Override
    public void removeMember(@NonNull QCMember existingMember) {
        mMembers.remove(existingMember);
        mCanStart.onNext(canStart());
    }

    @Override
    public int getMemberCount() {
        return mMembers.size();
    }

    @Override
    public void setContent(@NonNull String setName, @NonNull List<Pair<String, String>> content) {
        mSetName = setName;
        // TODO : don't allow duplicate values or values with ambigious results!!
        Log.i(TAG, "Content has been set : " + content.size() + " pairs");
        mContent = content;
        mCanStart.onNext(canStart());
    }

    private boolean canStart() {
        Log.v(TAG, "Checking to see if we can start... Players: " + mMembers.size() + " > 1 && " + mContent.size() + " > " + MIN_CONTENT_SIZE + " = ?");
        return mMembers.size() > 1 && mContent.size() > MIN_CONTENT_SIZE;
    }

    @NonNull
    @Override
    public String getQuestionForAnswer(@NonNull String answer) {
        for (Pair<String, String> pair : mContent) {
            if (answer.equals(pair.second)) {
                return pair.first;
            }
        }
        throw new IllegalStateException("Unable to find question for answer '" + answer + "'");
    }

    @NonNull
    @Override
    public String getAnswerForPlayer(@NonNull QCMember member) {
        String question = member.question();
        if (question == null) {
            throw new IllegalStateException("Player is not asking a question : " + member);
        }
        for (Pair<String, String> pair : mContent) {
            if (question.equals(pair.first)) {
                return pair.second;
            }
        }
        throw new IllegalStateException("Unable to find answer for question '" + question + "', asked by player " + member);
    }

    @NonNull
    @Override
    public List<QCMember> getPlayersWithAnswer(@NonNull String answer) {
        List<QCMember> result = new ArrayList<>();
        for (QCMember member : mMembers) {
            List<String> options = member.options();
            if (options == null) {
                continue;
            }
            for (String option : options) {
                if (answer.equals(option)) {
                    result.add(member);
                }
            }
        }
        return result;
    }

    @NonNull
    @Override
    public List<QCMember> askersLookingForAnswer(@NonNull String answer) {
        List<QCMember> result = new ArrayList<>();
        String question = null;
        for (Pair<String, String> pair : mContent) {    // first find the question to the answer
            if (answer.equals(pair.second)) {
                question = pair.first;
                break;
            }
        }
        if (question == null) {
            return result;
        }
        for (QCMember member : mMembers) {      // then find the users asking that question
            if (question.equals(member.question())) {
                result.add(member);
            }
        }
        return result;
    }

    @Override
    public void allocateContent() {
        mMembers = mDistributionLogic.allocateContent(4, new ArrayList<>(mMembers), mContent);
    }

    @Override
    public void updatePlayersForCorrectStatus(@NonNull QCMember asker, @NonNull QCMember answerer, @NonNull String answer) {
        mMembers = mDistributionLogic.updateForCorrectMove(asker, answerer, answer, new ArrayList<>(mMembers), mContent);
    }

    @Override
    public void updatePlayersForBadMove(@NonNull QCMember asker, @NonNull QCMember answerer, @NonNull String providedAnswer, @NonNull String correctAnswer, @NonNull List<QCMember> othersAtFault, @NonNull List<QCMember> askersOfAnswer) {
        mMembers = mDistributionLogic.updateForBadMove(asker, answerer, providedAnswer, correctAnswer, othersAtFault, askersOfAnswer, new ArrayList<>(mMembers), mContent);
    }
}
