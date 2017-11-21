package sixarmstudios.quizletcolors.logic.engine;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.example.myapplication.bluetooth.QCGameMessage;
import com.example.myapplication.bluetooth.QCMember;
import com.example.myapplication.bluetooth.QCPlayerMessage;

import java.util.List;

import io.reactivex.Observable;

/**
 * This is the interface that the Host implements/runs the logic for all incoming messages (be it
 * their own or from other devices)
 * <p>
 * This holds state and pairs new question & answers with users.
 */
public interface IGameEngine {
    // region Outside of logic consumable methods
    public QCGameMessage startGame(QCGameMessage.GameType gameType, Integer gameTarget);

    public QCGameMessage processMessage(@NonNull QCPlayerMessage message);

    public Observable<Boolean> isAbleToStart();

    public boolean isGameInProgress();
    // endregion

    @NonNull QCGameMessage generateBaseLobbyMessage(@NonNull QCGameMessage.Action action);

    @NonNull QCGameMessage generateBasePlayMessage(@NonNull QCGameMessage.Action action);

    @Nullable QCMember findMemberByUsername(@NonNull String username);

    @Nullable QCMember findMemberByColor(@NonNull String color);

    void addMember(@NonNull QCMember newMember);

    void removeMember(@NonNull QCMember existingMember);

    int getMemberCount();

    /**
     * This REPLACES all existing content with the new content list.
     * No filtering is done on the content to prevent buggy content from getting through
     * (Ex: multiple questions with matching answer)
     */
    void setContent(@NonNull String setName, @NonNull List<Pair<String, String>> content);

    @NonNull List<QCMember> getPlayersWithAnswer(@NonNull String answer);

    @NonNull List<QCMember> askersLookingForAnswer(@NonNull String answer);

    void allocateContent();

    @NonNull String getAnswerForPlayer(@NonNull QCMember member);

    @NonNull String getQuestionForAnswer(@NonNull String answer);

    void updatePlayersForCorrectStatus(@NonNull QCMember asker, @NonNull QCMember answerer, @NonNull String answer);

    /**
     * @param asker          user who the answer was sent to
     * @param answerer       user who sent the answer
     * @param providedAnswer the answer the user supplied
     * @param correctAnswer  the answer that SHOULD have been supplied to the `asker`
     * @param othersAtFault  other players who had the correctAnswer in their options list
     * @param askersOfAnswer other players who WANTED the provided answer
     */
    void updatePlayersForBadMove(@NonNull QCMember asker, @NonNull QCMember answerer,
                                 @NonNull String providedAnswer, @NonNull String correctAnswer,
                                 @NonNull List<QCMember> othersAtFault, @NonNull List<QCMember> askersOfAnswer);
}
