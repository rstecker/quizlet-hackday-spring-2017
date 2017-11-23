package sixarmstudios.quizletcolors.ui.board;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import sixarmstudios.quizletcolors.R;
import studioes.arm.six.partskit.BoardView;
import studioes.arm.six.partskit.CompassRose;
import studioes.arm.six.partskit.GradeBox;
import ui.BadMove;
import ui.Game;
import ui.GoodMove;
import ui.Option;
import ui.Player;
import viewmodel.BoardViewModel;

import static studioes.arm.six.partskit.CompassRose.getShapeBasedOnUsername;

/**
 * Created by rebeccastecker on 6/11/17.
 */

public class BoardFragment extends Fragment implements BoardView.IBoardListener {

    public static final String TAG = BoardFragment.class.getSimpleName();
    @LayoutRes
    private static final int LAYOUT_ID = R.layout.board_fragment;

    @BindView(R.id.board_view)
    BoardView mBoard;

    @BindView(R.id.grade_box)
    GradeBox mGrade;


    private String mPlayerColor;
    private String mString;
    private long mLastMoveUpdateTimestamp;
    private Disposable mGradeDisposable;

    public static BoardFragment newInstance() {
        BoardFragment fragment = new BoardFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(LAYOUT_ID, container, false);
        ButterKnife.bind(this, view);

        mBoard.setMoveCallback(this);
        mGrade.lockIt();

        mLastMoveUpdateTimestamp = new Date().getTime();
        BoardViewModel viewModel = ViewModelProviders.of(this).get(BoardViewModel.class);
        viewModel.getPlayers().observe(this, this::handlePlayerUpdates);
        viewModel.getGame().observe(this, this::handleGameUpdates);
        viewModel.getOptions().observe(this, this::handleOptionUpdates);
        viewModel.getMyBadMoves().observe(this, this::handleBadMoves);
        viewModel.getMyGoodMoves().observe(this, this::handleGoodMoves);

//        mBadMoveView = new BadMoveViewHolder(mWrongQuestionPopup);
        return view;
    }

    private void handleBadMoves(List<BadMove> badMoves) {
        if (badMoves == null || badMoves.size() == 0) {
            return;
        }
        BadMove move = badMoves.get(0);
        if (move.timestamp <= mLastMoveUpdateTimestamp) {
            return;
        }
        mLastMoveUpdateTimestamp = move.timestamp;

        Log.i(TAG, "Bad move update : " + move);

        if (move.youAnsweredPoorly) {
            mGrade.populateWrongAnswer(move.offeredAnswer, move.offeredAnswerColor, move.incorrectQuestion, move.incorrectQuestionColor, move.correctQuestion, move.correctQuestionColor);
            // current player owns offered answer
        } else if (move.youWereGivenBadAnswer) {
            mGrade.populateWrongAnswer(move.incorrectQuestion, move.incorrectQuestionColor, move.offeredAnswer, move.offeredAnswerColor, move.correctAnswer, move.correctAnswerColor);
            // current player owns incorrect question
        } else if (move.yourAnswerWentToSomeoneElse) {
            mGrade.populateWrongAnswer(move.offeredAnswer, move.offeredAnswerColor, move.incorrectQuestion, move.incorrectQuestionColor, move.correctQuestion, move.correctQuestionColor);
            // current player owns the "correct question"
        } else if (move.youFailedToAnswer) {
            mGrade.populateWrongAnswer(move.offeredAnswer, move.offeredAnswerColor, move.incorrectQuestion, move.incorrectQuestionColor, move.correctAnswer, move.correctAnswerColor);
            // current player owns "correct answer"
        }
        mGrade.popIt();
        if (mGradeDisposable != null) {
            mGradeDisposable.dispose();
        }
        mGradeDisposable = Single.timer(6, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (v) -> mGrade.lockIt(),
                        (e) -> Log.e(TAG, "Error encountered w/ grade " + e)
                );

    }

    private void handleGoodMoves(List<GoodMove> goodMoves) {
        if (goodMoves == null || goodMoves.size() == 0) {
            return;
        }
        GoodMove move = goodMoves.get(0);
        if (move.timestamp <= mLastMoveUpdateTimestamp) {
            return;
        }
        mLastMoveUpdateTimestamp = move.timestamp;
        Log.i(TAG, "Good move update : " + move);
        if (move.youAnswered && move.youAsked) {
            Toast.makeText(this.getContext(), "Good job!", Toast.LENGTH_SHORT).show();
        } else if (move.youAsked) {
            Toast.makeText(this.getContext(), "Your question has been answered correctly!", Toast.LENGTH_SHORT).show();
        } else if (move.youAnswered) {
            Toast.makeText(this.getContext(), "You answered correctly!", Toast.LENGTH_SHORT).show();
        }
        // TODO : rebecca :: these colors are blank!! wtf
        mBoard.reward(move.askerColor, move.answererColor, move.answer);
    }

    private void handlePlayerUpdates(List<Player> players) {
        Log.i(TAG, "I see players " + players);
        List<studioes.arm.six.partskit.Player> uiPlayers = new ArrayList<>();
        for (Player p : players) {
            // TODO : they shouldn't ALL be diamond lines... ?
            uiPlayers.add(new studioes.arm.six.partskit.Player(
                    p.username,
                    CompassRose.RoseColor.findByColorName(p.color),
                    p.score(),
                    p.isHost(),
                    p.isYou(),
                    getShapeBasedOnUsername(p.username)
            ));
        }
        mBoard.setPlayers(uiPlayers);
    }

    void handleOptionUpdates(List<Option> options) {
        if (options == null) {
            return;
        }
        List<String> strOptions = new ArrayList<>();
        List<String> curOptions = mBoard.getCurrentOptions();
        for (Option o : options) {
            Log.i(TAG, "I see incoming options : " + o.index + " :: " + o.option);
            strOptions.add(o.option);
        }
        for (String o : curOptions) {
            Log.i(TAG, "I see existing options : " + o);
        }
        for (int i = 0; i < strOptions.size(); ++i) {
            String s = strOptions.get(i);
            int preExistingIndex = curOptions.indexOf(s);
            if (preExistingIndex > -1 && i != preExistingIndex) {
                String temp = strOptions.get(preExistingIndex);
                Log.i(TAG, "Swaping option positions: " + s + " [" + i + "] <->" + temp + " [" + preExistingIndex + "]");
                strOptions.set(preExistingIndex, s);
                strOptions.set(i, temp);
                --i;    // DO IT AGAIN!!!
            } else {
                Log.i(TAG, "New value : " + s + " replaces " + curOptions.get(i));
            }
        }

        for (String o : strOptions) {
            Log.i(TAG, "I'm ending with options : " + o);
        }
        mBoard.setOptions(strOptions);
    }

    private void handleGameUpdates(List<Game> games) {
        if (games == null || games.size() != 1) {
            return;
        }
        Game game = games.get(0);
        Log.i(TAG, "I see game update " + game.selected_option + " / " + game.selected_color);
        // TODO : keep an eye on when one of those is null and the other is non null. That's a wonky state.
        mBoard.setQuestion(game.question);
    }

    @Override
    public void handleMove(String playerMove, String playerColor) {
        BoardViewModel viewModel = ViewModelProviders.of(this).get(BoardViewModel.class);
        viewModel.setPlayerMove(playerColor, playerMove);
    }
}
