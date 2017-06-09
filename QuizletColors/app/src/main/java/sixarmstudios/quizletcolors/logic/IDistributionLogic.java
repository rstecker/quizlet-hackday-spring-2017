package sixarmstudios.quizletcolors.logic;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.example.myapplication.bluetooth.QCMember;

import java.util.List;

/**
 * Created by rebeccastecker on 6/8/17.
 */

public interface IDistributionLogic {
    /**
     * Deals out a random number of content pairs to the members so that they all have handsize amount
     * Tries to not repeat but will repeat if it runs out of new content.
     *
     * @param members not modified
     * @param content not modified
     * @return new list of updated Member objects reflecting new options/questions
     */
    @NonNull List<QCMember> allocateContent(int handSize,
                                            @NonNull List<QCMember> members,
                                            @NonNull List<Pair<String, String>> content);

    /**
     * Gives the asker/answerer new content, leaves everyone else alone
     *
     * @param asker    has their question switched out
     * @param answerer has their answer switched out
     * @param answer   the correct answer/the answer submitted
     * @param members  not modified
     * @param content  not modified
     * @return new list of updated Member objects reflecting new options/questions
     */
    @NonNull List<QCMember> updateForCorrectMove(@NonNull QCMember asker,
                                                 @NonNull QCMember answerer,
                                                 @NonNull String answer,
                                                 @NonNull List<QCMember> members,
                                                 @NonNull List<Pair<String, String>> content);

    /**
     * Punishes players for a bad move:
     * <li> asker gets a new question
     * <li> askersOfAnswer (if any) get new questions
     * <li> answerer gets a new hand
     * <li> othersAtFault (if any) get new single term
     *
     * @param asker          user who the answer was sent to
     * @param answerer       user who sent the answer
     * @param providedAnswer the answer the user supplied
     * @param correctAnswer  the answer that SHOULD have been supplied to the `asker`
     * @param othersAtFault  other players who had the correctAnswer in their options list
     * @param askersOfAnswer other players who WANTED the provided answer
     * @param members        not modified
     * @param content        not modified
     * @return new list of updated Member objects reflecting new options/questions
     */
    @NonNull List<QCMember> updateForBadMove(@NonNull QCMember asker,
                                             @NonNull QCMember answerer,
                                             @NonNull String providedAnswer,
                                             @NonNull String correctAnswer,
                                             @NonNull List<QCMember> othersAtFault,
                                             @NonNull List<QCMember> askersOfAnswer,
                                             @NonNull List<QCMember> members,
                                             @NonNull List<Pair<String, String>> content);
}
