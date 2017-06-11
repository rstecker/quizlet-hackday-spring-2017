package sixarmstudios.quizletcolors.ui;

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
import ui.Game;
import ui.Player;

/**
 * Created by rebeccastecker on 6/10/17.
 */

public class LobbyFragment extends LifecycleFragment {
    public static final String TAG = LobbyFragment.class.getSimpleName();
    @LayoutRes public static final int LAYOUT_ID = R.layout.lobby_users_fragment;

    @BindView(R.id.lobby_users_text_field) TextView mUsers;
    @BindView(R.id.game_state_text_field) TextView mGame;
    @BindView(R.id.start_game_button) View mStartGameButton;

    public static LobbyFragment newInstance() {
        LobbyFragment fragment = new LobbyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(LAYOUT_ID, container, false);
        ButterKnife.bind(this, view);
        mUsers.setText("No users, but at least my fragment loaded?");

        mStartGameButton.setVisibility(View.INVISIBLE);
        LobbyViewModel lobbyViewModel = ViewModelProviders.of(this).get(LobbyViewModel.class);
        lobbyViewModel.getPlayers().observe(this, this::handlePlayerUpdates);
        lobbyViewModel.getGame().observe(this, this::handleGameUpdates);

        return view;
    }

    private void handleGameUpdates(List<Game> games) {
        if (games == null || games.size() == 0) {
            mGame.setText("There is no game");
            return;
        }
        if (games.size() > 1) {
            mGame.setText("We have more than 1 game. Omg "+games);
            return;
        }
        Game game = games.get(0);
        StringBuilder sb = new StringBuilder("I see a game w/ state " + game.getState() + "\n");
        if (game.isHost()) {
            sb.append("You are hosting : " + game.hostName);
        } else {
            sb.append("You have joined : " + game.hostName);
        }
        mGame.setText(sb);
        mStartGameButton.setVisibility(game.getState() == Game.State.CAN_START ? View.VISIBLE : View.INVISIBLE);
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
}
