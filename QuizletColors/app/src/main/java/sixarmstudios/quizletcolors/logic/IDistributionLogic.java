package sixarmstudios.quizletcolors.logic;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.example.myapplication.bluetooth.QCMember;

import java.util.List;
import java.util.Set;

/**
 * Created by rebeccastecker on 6/8/17.
 */

public interface IDistributionLogic {
    @NonNull Set<QCMember> allocateContent(@NonNull Set<QCMember> members,
                                           @NonNull List<Pair<String, String>> content);

    @NonNull Set<QCMember> updateForCorrectMove(@NonNull QCMember asker,
                                                @NonNull QCMember answerer,
                                                @NonNull String answer,
                                                @NonNull Set<QCMember> members,
                                                @NonNull List<Pair<String, String>> content);

    @NonNull Set<QCMember> updateForBadMove(@NonNull QCMember asker,
                                            @NonNull QCMember answerer,
                                            @NonNull String providedAnswer,
                                            @NonNull String correctAnswer,
                                            @NonNull List<QCMember> othersAtFault,
                                            @NonNull List<QCMember> askersOfAnswer,
                                            @NonNull Set<QCMember> members,
                                            @NonNull List<Pair<String, String>> content);
}
