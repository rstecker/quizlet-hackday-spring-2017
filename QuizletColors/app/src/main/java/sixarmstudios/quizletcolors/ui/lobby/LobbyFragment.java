package sixarmstudios.quizletcolors.ui.lobby;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sixarmstudios.quizletcolors.R;
import sixarmstudios.quizletcolors.StartActivity;
import sixarmstudios.quizletcolors.connections.HostServiceConnection;
import sixarmstudios.quizletcolors.ui.board.IUserSelector;
import sixarmstudios.quizletcolors.ui.player.PlayerAdapter;
import ui.Fact;
import ui.Game;
import ui.SetSummary;
import viewmodel.LobbyViewModel;
import ui.Player;

/**
 * Created by rebeccastecker on 6/10/17.
 */
@ParametersAreNonnullByDefault
public class LobbyFragment extends LifecycleFragment implements IUserSelector {
    public static final String TAG = LobbyFragment.class.getSimpleName();
    @LayoutRes public static final int LAYOUT_ID = R.layout.lobby_users_fragment;
    private static final String SET_ID_ARG = "setIdArg";

    @BindView(R.id.lobby_users_text_field) TextView mUsers;
    @BindView(R.id.game_state_text_field) TextView mGameTextField;
    @BindView(R.id.start_game_button) View mStartGameButton;
    @BindView(R.id.player_list) RecyclerView mPlayerList;

    private PlayerAdapter mAdapter;
    private HostServiceConnection mHostConnection;

    public static LobbyFragment newInstance(@Nullable Long qSetId) {
        LobbyFragment fragment = new LobbyFragment();
        Bundle args = new Bundle();
        if (qSetId != null) {
            args.putLong(SET_ID_ARG, qSetId);
        }
        fragment.setArguments(args);
        return fragment;
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
        long qSetId = getArguments().getLong(SET_ID_ARG, 0);
        if (qSetId > 0) {
            lobbyViewModel.getSetSummary(qSetId).observe(this, this::handleSetUpdates);
            lobbyViewModel.getFacts(qSetId).observe(this, this::handleFactsUpdates);
        }

        mAdapter = new PlayerAdapter(this);
        mPlayerList.setAdapter(mAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false);
        mPlayerList.setLayoutManager(layoutManager);

        return view;
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        mHostConnection = ((StartActivity) context).getHostConnection();
    }

    @OnClick(R.id.start_game_button)
    public void handleStartClick() {
        LobbyViewModel lobbyViewModel = ViewModelProviders.of(this).get(LobbyViewModel.class);
        lobbyViewModel.setGameState(Game.State.START);
        mHostConnection.startGame();
    }

    private void handleSetUpdates(List<SetSummary> summaries) {
        if (summaries.isEmpty()) {
            return;
        }
        Log.i(TAG, "I see a set summary " + summaries.get(0));
    }

    private void handleFactsUpdates(List<Fact> facts) {
        if (facts.isEmpty()) {
            return;
        }
        Log.i(TAG, "I see " + facts.size());
        mHostConnection.setContent(facts);
    }

    private void handleGameUpdates(List<Game> games) {
        if (games == null || games.size() == 0) {
            mGameTextField.setText("There is no game");
            return;
        }
        if (games.size() > 1) {
            mGameTextField.setText("We have more than 1 game. Omg " + games);
            return;
        }
        Game game = games.get(0);
        StringBuilder sb = new StringBuilder("I see a game w/ state " + game.getState().toString() + " that ");
        if (game.isHost()) {
            sb.append("you are hosting : ");
        } else {
            sb.append("you have joined : ");
        }
        sb.append(game.hostName);
        mGameTextField.setText(sb.toString());
        mStartGameButton.setVisibility(game.getState() == Game.State.CAN_START ? View.VISIBLE : View.INVISIBLE);
    }

    private void handlePlayerUpdates(List<Player> players) {
        Log.w(TAG, "I see players " + players);
        if (players == null || players.size() == 0) {
            mUsers.setText("Lobby is empty");
            return;
        }
        mAdapter.setPlayers(players);
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

    @Override public void playerClicked(@NonNull String playerColor) {
    }
}
