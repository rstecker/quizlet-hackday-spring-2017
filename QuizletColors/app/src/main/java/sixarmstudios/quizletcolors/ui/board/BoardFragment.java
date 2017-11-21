package sixarmstudios.quizletcolors.ui.board;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
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
import studioes.arm.six.partskit.CompasRose;
import studioes.arm.six.partskit.GradeBox;
import ui.BadMove;
import ui.Game;
import ui.GoodMove;
import ui.Option;
import ui.Player;
import viewmodel.BoardViewModel;

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
//        viewModel.getMyGoodMoves().observe(this, this::handleGoodMoves);

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
            mGrade.populateWrongAnswer(move.offeredAnswer, move.incorrectQuestion, move.correctQuestion);
            mGrade.populateBlame();
            // current player owns offered answer
        } else if (move.youWereGivenBadAnswer) {
            mGrade.populateWrongAnswer(move.incorrectQuestion, move.offeredAnswer, move.correctAnswer);
            mGrade.populateBlame();
            // current player owns incorrect question
        } else if (move.yourAnswerWentToSomeoneElse) {
            mGrade.populateWrongAnswer(move.offeredAnswer, move.incorrectQuestion, move.correctQuestion);
            mGrade.populateBlame();
            // current player owns the "correct question"
        } else if (move.youFailedToAnswer) {
            mGrade.populateWrongAnswer(move.offeredAnswer, move.incorrectQuestion, move.correctAnswer);
            mGrade.populateBlame();
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
                        (e) -> Log.e(TAG, "Error encountered w/ grade "+e)
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
    }

    private void handlePlayerUpdates(List<Player> players) {
        Log.w(TAG, "I see players " + players);
        List<Pair<CompasRose.RoseColor, Integer>> uiPlayers = new ArrayList<>();
        for (Player p : players) {
            // TODO : they shouldn't ALL be diamond lines... ?
            uiPlayers.add(new Pair<>(CompasRose.RoseColor.findByColorName(p.color), R.drawable.line_dimond));
        }
        mBoard.setPlayers(uiPlayers);
    }

    private void handleOptionUpdates(List<Option> options) {
        Log.w(TAG, "I see options " + options);
        if (options == null) {
            return;
        }
        List<String> strOptions = new ArrayList<>();
        for (Option o : options) {
            Log.i(TAG, "I see options : " + o.index + " :: " + o.option);
            strOptions.add(o.option);
        }
        List<String> curOptions = mBoard.getCurrentOptions();
        for (int i = 0; i < strOptions.size(); ++i) {
            String s = strOptions.get(i);
            int preExistingIndex = curOptions.indexOf(s);
            if (preExistingIndex > -1 && i != preExistingIndex) {
                String temp = strOptions.get(preExistingIndex);
                Log.i(TAG, "Swaping option positions: " + s + " [" + i + "] <->" + temp + " [" + preExistingIndex + "]");
                strOptions.set(preExistingIndex, s);
                strOptions.set(i, temp);
            } else {
                Log.i(TAG, "Removed option : " + s);
            }
        }
        mBoard.setOptions(strOptions);
    }

    private void handleGameUpdates(List<Game> games) {
        if (games == null || games.size() != 1) {
            return;
        }
        Game game = games.get(0);
        Log.w(TAG, "I see game update " + game.selected_option + " / " + game.selected_color);
        mBoard.setQuestion(game.question);
    }

    @Override
    public void handleMove(String playerMove, String playerColor) {
        BoardViewModel viewModel = ViewModelProviders.of(this).get(BoardViewModel.class);
        viewModel.setSubmittedOption(null);
        viewModel.setSelectedPlayer(null);
        viewModel.setSubmittedOption(playerMove);
        viewModel.setSelectedPlayer(playerColor);
    }
}
