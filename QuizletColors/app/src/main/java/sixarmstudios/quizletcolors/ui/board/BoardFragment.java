package sixarmstudios.quizletcolors.ui.board;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import sixarmstudios.quizletcolors.R;
import sixarmstudios.quizletcolors.ui.player.PlayerAdapter;
import ui.BadMove;
import ui.Game;
import ui.GoodMove;
import ui.Option;
import ui.Player;
import viewmodel.BoardViewModel;

/**
 * Created by rebeccastecker on 6/11/17.
 */

public class BoardFragment extends LifecycleFragment implements IUserSelector, IOptionSelector {

    public static final String TAG = BoardFragment.class.getSimpleName();
    @LayoutRes public static final int LAYOUT_ID = R.layout.board_fragment;

    @BindView(R.id.board_question) TextView mQuestion;
    @BindView(R.id.player_list) RecyclerView mPlayerList;
    @BindView(R.id.option_list) RecyclerView mOptionList;

    private PlayerAdapter mPlayerAdapter;
    private OptionAdapter mOptionAdapter;
    private String mPlayerColor;
    private String mString;
    private long mLastMoveUpdateTimestamp;
    private GridLayoutManager mOptionsLayoutManager;

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

        mLastMoveUpdateTimestamp = new Date().getTime();
        BoardViewModel viewModel = ViewModelProviders.of(this).get(BoardViewModel.class);
        viewModel.getPlayers().observe(this, this::handlePlayerUpdates);
        viewModel.getGame().observe(this, this::handleGameUpdates);
        viewModel.getOptions().observe(this, this::handleOptionUpdates);
        viewModel.getMyBadMoves().observe(this, this::handleBadMoves);
        viewModel.getMyGoodMoves().observe(this, this::handleGoodMoves);

        mPlayerAdapter = new PlayerAdapter(this);
        mPlayerList.setAdapter(mPlayerAdapter);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false);
        mPlayerList.setLayoutManager(layoutManager1);


        mOptionAdapter = new OptionAdapter(this);
        mOptionList.setAdapter(mOptionAdapter);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mOptionsLayoutManager = new GridLayoutManager(getContext(), 1, LinearLayoutManager.VERTICAL, false);
        } else {
            mOptionsLayoutManager = new GridLayoutManager(getContext(), 1, LinearLayoutManager.HORIZONTAL, false);
        }
        mOptionList.setLayoutManager(mOptionsLayoutManager);

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
        Log.i(TAG, "Bad move update : "+move);
        if (move.youAnsweredPoorly) {
            Toast.makeText(this.getContext(), "You submitted the wrong answer", Toast.LENGTH_SHORT).show();
        } else if (move.youWereGivenBadAnswer) {
            Toast.makeText(this.getContext(), "Your question was incorrectly answered", Toast.LENGTH_SHORT).show();
        } else if (move.youFailedToAnswer) {
            Toast.makeText(this.getContext(), "You failed to help someone out", Toast.LENGTH_SHORT).show();
        } else if (move.yourAnswerWentToSomeoneElse) {
            Toast.makeText(this.getContext(), "Your correct answer went to the wrong person", Toast.LENGTH_SHORT).show();
        }

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
        Log.i(TAG, "Good move update : "+move);
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
        mPlayerAdapter.setPlayers(players);
    }

    private void handleOptionUpdates(List<Option> options) {
        Log.w(TAG, "I see options " + options);
        if (options == null) {
            return;
        }
        mOptionsLayoutManager.setSpanCount(Math.max(1,options.size()));
        mOptionAdapter.setOptions(options);
    }

    private void handleGameUpdates(List<Game> games) {
        if (games == null || games.size() != 1) {
            return;
        }

        Game game = games.get(0);
        Log.w(TAG, "I see game update " + game.selected_option + " / " + game.selected_color);
        mQuestion.setText(game.question);
        mQuestion.setTag(game.question);
        mPlayerAdapter.setSelectedPlayer(game.selected_color);
        mPlayerColor = game.selected_color;
        mOptionAdapter.setSelectedOption(game.selected_option);
        mString = game.selected_option;
    }

    @Override public void playerClicked(@NonNull String playerColor) {
        BoardViewModel viewModel = ViewModelProviders.of(this).get(BoardViewModel.class);
        if (playerColor.equals(mPlayerColor)) {
            viewModel.setSelectedPlayer(null);
            mPlayerColor = null;
        } else {
            viewModel.setSelectedPlayer(playerColor);
            mPlayerColor = playerColor;
        }
    }

    @Override public void optionClicked(@NonNull String optionText) {
        BoardViewModel viewModel = ViewModelProviders.of(this).get(BoardViewModel.class);
        if (optionText.equals(mString)) {
            viewModel.setSubmittedOption(null);
            mString = null;
        } else {
            viewModel.setSubmittedOption(optionText);
            mString = optionText;
        }
    }
}
