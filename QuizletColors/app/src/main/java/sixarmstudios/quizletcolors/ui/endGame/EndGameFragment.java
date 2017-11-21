package sixarmstudios.quizletcolors.ui.endGame;

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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sixarmstudios.quizletcolors.R;
import sixarmstudios.quizletcolors.ui.scorePlayer.ScorePlayerAdapter;
import sixarmstudios.quizletcolors.ui.setup.StartFragment;
import ui.Player;

/**
 * Created by austinrobarts on 11/21/17.
 */

public class EndGameFragment extends Fragment {
    @LayoutRes private static final int LAYOUT_RES = R.layout.fragment_end_game;

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
        List<Player> players = new ArrayList<>();
        players.add(new Player("Aust", null, true, true, 10));
        players.add(new Player("Bost", null, false, false, 6));
        players.add(new Player("Faust", null, false, false, 8));
        players.add(new Player("Joust", null, false, false, 9));
        mAdapter = new ScorePlayerAdapter(players);
        mScoreList.setAdapter(mAdapter);
        mScoreList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        return view;
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
