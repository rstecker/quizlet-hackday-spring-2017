package sixarmstudios.quizletcolors.ui.endGame;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sixarmstudios.quizletcolors.R;
import sixarmstudios.quizletcolors.ui.scorePlayer.ScorePlayerAdapter;
import sixarmstudios.quizletcolors.ui.setup.StartFragment;
import studioes.arm.six.partskit.CompassRose;
import ui.Player;
import viewmodel.BoardViewModel;

/**
 * Created by austinrobarts on 11/21/17.
 */

public class EndGameFragment extends Fragment {
    @LayoutRes private static final int LAYOUT_RES = R.layout.fragment_end_game;
    public static final String TAG = EndGameFragment.class.getSimpleName();

    @BindView(R.id.game_winner_announce) TextView mGameWinner;
    @BindView(R.id.player_scores_view) RecyclerView mScoreList;
    @BindView(R.id.score_main_menu_button) Button mMainMenuButton;

    private ScorePlayerAdapter mAdapter;

    public static Fragment newInstance() {
        Fragment fragment = new EndGameFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(LAYOUT_RES, container, false);
        ButterKnife.bind(this, view);

        BoardViewModel viewModel = ViewModelProviders.of(this).get(BoardViewModel.class);
        viewModel.getPlayers().observe(this, this::handlePlayers);
        mAdapter = new ScorePlayerAdapter();
        mScoreList.setAdapter(mAdapter);
        mScoreList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        return view;
    }

    private void handlePlayers(List<Player> players) {
        List<studioes.arm.six.partskit.Player> uiPlayers = new ArrayList<>();
        for (Player p : players) {
            uiPlayers.add(new studioes.arm.six.partskit.Player(
                    p.username,
                    CompassRose.RoseColor.findByColorName(p.color),
                    p.score(),
                    p.isHost(),
                    p.isYou(),
                    R.drawable.line_dimond
            ));
        }
        mAdapter.setPlayers(uiPlayers);
        mGameWinner.setText(String.format(getResources().getString(R.string.game_winner_string), mAdapter.getWinner()));
    }


    @OnClick(R.id.score_main_menu_button)
    protected void onMainMenuClick() {
        //go back to start fragment
        Fragment startFragment = StartFragment.newInstance();
        //this is wrong but too lazy to figure out view model stuff
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, startFragment)
                .commit();
    }
}
