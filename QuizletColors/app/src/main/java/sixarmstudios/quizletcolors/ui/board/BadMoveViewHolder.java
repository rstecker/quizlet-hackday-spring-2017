package sixarmstudios.quizletcolors.ui.board;

import android.support.annotation.LayoutRes;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import sixarmstudios.quizletcolors.R;
import ui.BadMove;

/**
 * Created by sithel on 9/10/17.
 */

public class BadMoveViewHolder {
    public static final String TAG = BoardFragment.class.getSimpleName();
    @LayoutRes public static final int LAYOUT_ID = R.layout.view_bad_move;

    Disposable mBadMoveDisposable;
    @BindView(R.id.wrong_question_details) TextView mWrongQuestionDetails;
    @BindView(R.id.big_window) View mWindow;

    public BadMoveViewHolder(FrameLayout parent) {
        View view = View.inflate(parent.getContext(), LAYOUT_ID, parent);
        ButterKnife.bind(this, view);
    }

    public void handleNewBadMove(BadMove move) {
        if (mBadMoveDisposable != null) {
            mBadMoveDisposable.dispose();
        }
        StringBuilder sb = new StringBuilder();
        if (move.youAnsweredPoorly) {
            sb.append("You offered ").append(move.offeredAnswer).append("\n");
            sb.append("as the answer to ").append(move.correctQuestion).append("\n");
            sb.append("when actually ").append(move.correctAnswer).append(" is the correct answer").append("\n");
            sb.append("\n");
            sb.append("The question that goes with the answer you provided is ").append(move.incorrectQuestion);
        } else if (move.youWereGivenBadAnswer) {
            sb.append("Your question of ").append(move.correctQuestion).append("\n");
            sb.append("has an answer of ").append(move.correctAnswer).append("\n");
            sb.append("but the answer ").append(move.offeredAnswer).append("\n");
            sb.append("was provided instead");
        } else if (move.youFailedToAnswer) {
            sb.append("Someone was asking ").append(move.correctQuestion).append("\n");
            sb.append("and you had the answer of ").append(move.correctAnswer).append("\n");
            sb.append("but didn't give it to them in time");
        } else if (move.yourAnswerWentToSomeoneElse) {
            sb.append("The answer to your question ").append(move.correctQuestion).append("\n");
            sb.append("was ").append(move.correctQuestion).append("\n");
            sb.append("but it was given to a different user");
        }
        mWrongQuestionDetails.setText(sb.toString());
        mWindow.setVisibility(View.VISIBLE);
        mBadMoveDisposable = Single.timer(10, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((t) -> {
                    mWindow.setVisibility(View.GONE);
                });
    }
}
