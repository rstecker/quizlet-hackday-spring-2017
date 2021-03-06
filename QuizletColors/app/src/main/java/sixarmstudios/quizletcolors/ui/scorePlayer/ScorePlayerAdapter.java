package sixarmstudios.quizletcolors.ui.scorePlayer;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import studioes.arm.six.partskit.CompassRose;
import studioes.arm.six.partskit.Player;

/**
 * Created by austinrobarts on 11/21/17.
 */

public class ScorePlayerAdapter extends RecyclerView.Adapter<ScorePlayerViewHolder> {

    private List<Player> mPlayers = new ArrayList<>();

    public void setPlayers(@NonNull List<Player> players) {
        Collections.sort(players, new Comparator<Player>() {
            @Override
            public int compare(Player player, Player t1) {
                return t1.getScore() - player.getScore();
            }
        });
        this.mPlayers = players;
        notifyDataSetChanged();
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
        holder.setPlayerInfo(player.getUsername(), position + 1, player.getScore(), player.getColor());
    }

    private boolean isValidIdx(int position) {
        return mPlayers.size() <= position && position >= 0;
    }

    public String getWinner() {
        if (mPlayers.size() > 0) {
            return mPlayers.get(0).getUsername();
        }
        return "";
    }

    @Override
    public int getItemCount() {
        return mPlayers.size();
    }
}
