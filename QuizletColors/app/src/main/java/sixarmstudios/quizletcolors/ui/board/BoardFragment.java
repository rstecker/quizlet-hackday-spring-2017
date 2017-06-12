package sixarmstudios.quizletcolors.ui.board;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import sixarmstudios.quizletcolors.R;
import ui.BoardViewModel;
import ui.Game;
import ui.Player;

/**
 * Created by rebeccastecker on 6/11/17.
 */

public class BoardFragment extends LifecycleFragment {

    public static final String TAG = BoardFragment.class.getSimpleName();
    @LayoutRes public static final int LAYOUT_ID = R.layout.board_fragment;

    @BindView(R.id.board_question) TextView mQuestion;
    @BindView(R.id.board_option_1) TextView mOption1;
    @BindView(R.id.board_option_2) TextView mOption2;
    @BindView(R.id.board_option_3) TextView mOption3;
    @BindView(R.id.board_option_4) TextView mOption4;

    @BindView(R.id.lobby_users_text_field) TextView mUsers;

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

        BoardViewModel viewModel = ViewModelProviders.of(this).get(BoardViewModel.class);
        viewModel.getPlayers().observe(this, this::handlePlayerUpdates);
        viewModel.getGame().observe(this, this::handleGameUpdates);

        return view;
    }

    private void handlePlayerUpdates(List<Player> players) {
        Log.w(TAG, "I see players " + players);
        if (players == null || players.size() == 0) {
            mUsers.setText("Lobby is empty");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Player player : players) {
            sb.append(player.username);
            if (StringUtils.isNotEmpty(player.color)) {
                sb.append(" (" + player.color + ") ");
            }
            if (player.isYou()) {
                sb.append("[You!]");
            }
            if (player.isHost()) {
                sb.append("[Host]");
            }
            sb.append("\n");
        }
        mUsers.setText(sb.toString());
    }

    private void handleGameUpdates(List<Game> games) {
        if (games == null || games.size() != 1) {
            return;
        }
        Game game = games.get(0);

        mQuestion.setText(game.question);
        mOption1.setText(game.answerOption1);
        mOption2.setText(game.answerOption2);
        mOption3.setText(game.answerOption3);
        mOption4.setText(game.answerOption4);
    }
}
