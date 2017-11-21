package sixarmstudios.quizletcolors.ui.scorePlayer;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import ui.Player;

/**
 * Created by austinrobarts on 11/21/17.
 */

public class ScorePlayerAdapter extends RecyclerView.Adapter<ScorePlayerViewHolder> {

    private List<Player> mPlayers;

    public ScorePlayerAdapter(@NonNull List<Player> players) {
        Collections.sort(players, new Comparator<Player>() {
            @Override
            public int compare(Player player, Player t1) {
                return t1.score - player.score;
            }
        });
        this.mPlayers = players;
    }

    @Override
    public ScorePlayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(ScorePlayerViewHolder.LAYOUT_ID, parent, false);
        return new ScorePlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ScorePlayerViewHolder holder, int position) {
        if (isValidIdx(position)) {
            return;
        }
        Player player = mPlayers.get(position);
        holder.setPlayerInfo(player.username, position + 1, player.score);
    }

    private boolean isValidIdx(int position) {
        return mPlayers.size() <= position && position >= 0;
    }

    @Override
    public int getItemCount() {
        return mPlayers.size();
    }
}
