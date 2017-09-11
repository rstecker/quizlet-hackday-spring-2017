package sixarmstudios.quizletcolors.ui.board;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import javax.annotation.ParametersAreNonnullByDefault;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import sixarmstudios.quizletcolors.R;
import ui.BadMove;

/**
 * Created by sithel on 9/10/17.
 */
@ParametersAreNonnullByDefault
public class BadMoveViewHolder {
    public static final String TAG = BoardFragment.class.getSimpleName();
    @LayoutRes public static final int LAYOUT_ID = R.layout.view_bad_move;
    private static final int BAD_MOVE_COOL_DOWN = 5;

    Disposable mBadMoveDisposable;
    @BindView(R.id.bad_move_top_text) TextView mTopTxt;
    @BindView(R.id.bad_move_mid_text) TextView mMidTxt;
    @BindView(R.id.bad_move_bottom_text) TextView mBottomTxt;

    @BindView(R.id.bad_move_top_q) TextView mTopQ;
    @BindView(R.id.bad_move_top_a) TextView mTopA;
    @BindView(R.id.bad_move_mid_q) TextView mMidQ;
    @BindView(R.id.bad_move_mid_a) TextView mMidA;
    @BindView(R.id.bad_move_bottom_q) TextView mBottomQ;
    @BindView(R.id.bad_move_bottom_a) TextView mBottomA;

    @BindView(R.id.bad_move_timer) TextView mCountdown;

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
        sb.append("youAnsweredPoorly [" + move.youAnsweredPoorly + "]\n");
        sb.append("youWereGivenBadAnswer [" + move.youWereGivenBadAnswer + "]\n");
        sb.append("youWereGivenBadAnswer [" + move.youWereGivenBadAnswer + "]\n");
        sb.append("youFailedToAnswer [" + move.youFailedToAnswer + "]\n");
        sb.append("correctQuestion [" + move.correctQuestion + "]\n");
        sb.append("correctAnswer [" + move.correctAnswer + "]\n");
        sb.append("incorrectQuestion [" + move.incorrectQuestion + "]\n");
        sb.append("offeredAnswer [" + move.offeredAnswer + "]\n");
        Log.i(TAG, sb.toString());
        if (move.youAnsweredPoorly) {
            setDeets(mTopTxt, "You said");
            setDeets(mTopQ, move.incorrectQuestion);
            setDeets(mTopA, move.offeredAnswer);
            setDeets(mMidTxt, "the correct pairing is");
            setDeets(mMidQ, move.incorrectQuestion);
            setDeets(mMidA, move.correctAnswer);
            setDeets(mBottomTxt, null);
            setDeets(mBottomQ, move.correctQuestion);
            setDeets(mBottomA, move.offeredAnswer);
        } else if (move.youWereGivenBadAnswer) {
            setDeets(mTopTxt, "The pairing you needed was");
            setDeets(mTopQ, move.incorrectQuestion);
            setDeets(mTopA, move.correctAnswer);
            setDeets(mMidTxt, "but sadly someone thought");
            setDeets(mMidQ, move.incorrectQuestion);
            setDeets(mMidA, move.offeredAnswer);
            setDeets(mBottomTxt, "went together. Which it doesn't");
            setDeets(mBottomQ, null);
            setDeets(mBottomA, null);
        } else if (move.youFailedToAnswer) {
            setDeets(mTopTxt, "You had the answer but failed to help your friend out!");
            setDeets(mTopQ, move.incorrectQuestion);
            setDeets(mTopA, move.correctAnswer);
            setDeets(mMidTxt, "is meant to be together!");
            setDeets(mMidQ, null);
            setDeets(mMidA, null);
            setDeets(mBottomTxt, null);
            setDeets(mBottomQ, null);
            setDeets(mBottomA, null);
        } else if (move.yourAnswerWentToSomeoneElse) {
            setDeets(mTopTxt, "Your answer went to someone else!");
            setDeets(mTopQ, null);
            setDeets(mTopA, null);
            setDeets(mMidTxt, "You should speak up! You were looking for ");
            setDeets(mMidQ, move.incorrectQuestion);
            setDeets(mMidA, move.correctAnswer);
            setDeets(mBottomTxt, "but someone got confused...");
            setDeets(mBottomQ, null);
            setDeets(mBottomA, null);
        }
        mWindow.setVisibility(View.VISIBLE);
        mBadMoveDisposable = Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe((s) -> {
                    mCountdown.setText(String.valueOf(BAD_MOVE_COOL_DOWN));
                })
                .doOnNext((count) -> {
                    mCountdown.setText(String.valueOf(BAD_MOVE_COOL_DOWN - count - 1));
                })
                .take(BAD_MOVE_COOL_DOWN)
                .subscribe((t) -> {
                }, (e) -> Log.e(TAG, "Error " + e), () -> {
                    mWindow.setVisibility(View.GONE);
                });
    }

    private void setDeets(TextView v, @Nullable String text) {
        v.setVisibility(text == null ? View.GONE : View.VISIBLE);
        if (text != null) {
            v.setText(text);
        }
    }
}
