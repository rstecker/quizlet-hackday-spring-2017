package sixarmstudios.quizletcolors.ui.lobby;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.myapplication.bluetooth.QCGameMessage;

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
import ui.Player;
import ui.SetSummary;
import viewmodel.LobbyViewModel;

/**
 * Where the users sit and wait till the game starts. Passive players can't do anything. The Host
 * can do the following:
 * <li> start the game when the conditions are met (set & 2+ players)
 * <li> (TODO) select the game mode
 * <li> (TODO) kick of a broadcast
 * The lobby should display to everyone the following information:
 * <li> (TODO) Name of set & number of "facts"
 * <li> All current players
 */
@ParametersAreNonnullByDefault
public class LobbyFragment extends Fragment implements IUserSelector {
    public static final String TAG = LobbyFragment.class.getSimpleName();
    @LayoutRes public static final int LAYOUT_ID = R.layout.lobby_users_fragment;
    private static final String SET_ID_ARG = "setIdArg";

    @BindView(R.id.game_state_text_field) TextView mGameTextField;
    @BindView(R.id.game_set_title) TextView mSetTitle;
    @BindView(R.id.game_set_fact_count) TextView mFactCount;
    @BindView(R.id.start_game_button) View mStartGameButton;
    @BindView(R.id.player_list) RecyclerView mPlayerList;
    @BindView(R.id.target_elements) View mTargetElements;
    @BindView(R.id.target_number_picker) EditText mTargetChoice;
    @BindView(R.id.target_entry_description) TextView mTargetDescription;
    @BindView(R.id.game_type_selector) RadioGroup mGameTypeSelector;


    private PlayerAdapter mAdapter;
    private GridLayoutManager mPlayersLayoutManager;
    private HostServiceConnection mHostConnection;
    private QCGameMessage.GameType gameType = QCGameMessage.GameType.INFINITE;

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

        mStartGameButton.setVisibility(View.INVISIBLE);
        mGameTypeSelector.setOnCheckedChangeListener((radioGroup, checkedId) -> {

            switch (checkedId) {
                case R.id.infinity_button:
                    //infinity game
                    gameType = QCGameMessage.GameType.INFINITE;
                    mTargetElements.setVisibility(View.INVISIBLE);
                    break;
                case R.id.to_player_points_button:
                    //first player to target points
                    gameType = QCGameMessage.GameType.FIRST_PLAYER_TO_POINTS;
                    mTargetDescription.setText("Player needs to score this many points to win");
                    mTargetElements.setVisibility(View.VISIBLE);
                    break;
                case R.id.to_all_points_button:
                    //all players to target points
                    gameType = QCGameMessage.GameType.ALL_PLAYERS_TO_POINTS;
                    mTargetDescription.setText("All players need to score this many points to win");
                    mTargetElements.setVisibility(View.VISIBLE);
                    break;
                case R.id.to_minutes_button:
                    //certain number of minutes
                    gameType = QCGameMessage.GameType.TIMED_GAME;
                    mTargetDescription.setText("Game will continue for this many minutes");
                    mTargetElements.setVisibility(View.VISIBLE);
                    break;
            }

        });
        mGameTypeSelector.check(R.id.to_player_points_button);
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
        mPlayersLayoutManager = new GridLayoutManager(getContext(), 1, LinearLayoutManager.VERTICAL, false);
        mPlayerList.setLayoutManager(mPlayersLayoutManager);

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

        // TODO : protect against users clicking host before these game type info is set
        // TODO : protect against missing N values (or no N when we don't care)
        String targetStr = mTargetChoice.getText().toString();
        Log.i(TAG, "Starting game, I see selected "+gameType+"  && "+targetStr);
        if (StringUtils.isEmpty(targetStr)) {
            targetStr = "6";
        }
        mHostConnection.startGame(gameType, Integer.valueOf(targetStr));
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
        // FIXME : assumes all the facts are from the same single set
        mHostConnection.setContent(facts.get(0).qSetName, facts);
    }

    private void handleGameUpdates(List<Game> games) {
        if (games.size() == 0) {
            mGameTextField.setText("There is no game");
            return;
        }
        if (games.size() > 1) {
            Log.e(TAG, "Somehow more than 1 game found. This isn't right "+games.size());
        }
        Game game = games.get(0);
        StringBuilder sb = new StringBuilder("I see a game w/ state " + game.getState().toString() + " that ");
        if (game.isHost()) {
            sb.append("you are hosting : ");
            mGameTypeSelector.setVisibility(View.VISIBLE);
        } else {
            sb.append("you have joined : ");
        }
        sb.append(game.hostName);
        mGameTextField.setText(sb.toString());
        if (StringUtils.isNotEmpty(game.qSetName)) {
            mSetTitle.setText(game.qSetName);
            mFactCount.setText(String.valueOf(game.factCount));
        }
        mStartGameButton.setVisibility(game.getState() == Game.State.CAN_START ? View.VISIBLE : View.INVISIBLE);
    }

    private void handlePlayerUpdates(List<Player> players) {
        Log.w(TAG, "I see players " + players);
        if (players.size() == 0) {
            return;
        }
        mPlayersLayoutManager.setSpanCount(Math.max(1, players.size()));
        mAdapter.setPlayers(players);
    }

    @Override public void playerClicked(@NonNull String playerColor) { }
}
