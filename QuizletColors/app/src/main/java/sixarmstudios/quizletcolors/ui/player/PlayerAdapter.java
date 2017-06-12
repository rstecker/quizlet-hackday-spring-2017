package sixarmstudios.quizletcolors.ui.player;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import sixarmstudios.quizletcolors.ui.board.IUserSelector;
import ui.Player;

/**
 * Created by rebeccastecker on 6/11/17.
 */

public class PlayerAdapter extends RecyclerView.Adapter<PlayerViewHolder> {
    private List<Player> mPlayers;
    private String mSelectedColor;
    private IUserSelector mSelector;

    public PlayerAdapter(IUserSelector selector) {
        mSelector = selector;
        mPlayers = new ArrayList<>();
    }

    public void setPlayers(List<Player> players) {
        if (players == null || players.size() < mPlayers.size()) {
            return;
        }
        mPlayers = players;
        notifyDataSetChanged(); // TODO : ID the changed players and animate their update
    }

    @Override public PlayerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(PlayerViewHolder.LAYOUT_ID, viewGroup, false);
        return new PlayerViewHolder(view, mSelector);
    }

    @Override public void onBindViewHolder(PlayerViewHolder playerViewHolder, int i) {
        if (isInvalidIndex(i)) {
            return;
        }
        Player player = mPlayers.get(i);
        playerViewHolder.setPlayerData(player, player.color.equals(mSelectedColor));
    }

    @Override public int getItemCount() {
        return mPlayers.size();
    }

    private boolean isInvalidIndex(int i) {
        return i < 0 && i >= mPlayers.size();
    }


    public void setSelectedPlayer(String newSelectedColor) {
        String oldSelectedColor = mSelectedColor;
        mSelectedColor = newSelectedColor;
        int oldIndex = -1;
        int newIndex = -1;
        for (int i = 0; i < mPlayers.size(); ++i) {
            if (mPlayers.get(i).color.equals(oldSelectedColor)) {
                oldIndex = i;
            }
            if (mPlayers.get(i).color.equals(newSelectedColor)) {
                newIndex = i;
            }
        }
        notifyItemChanged(oldIndex);
        notifyItemChanged(newIndex);
    }
}
