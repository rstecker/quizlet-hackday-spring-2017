package sixarmstudios.quizletcolors.ui.player;

import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import sixarmstudios.quizletcolors.R;
import sixarmstudios.quizletcolors.logic.player.CreatureCreation;
import sixarmstudios.quizletcolors.ui.board.IUserSelector;
import ui.Player;

/**
 * Created by rebeccastecker on 6/11/17.
 */

public class PlayerViewHolder extends RecyclerView.ViewHolder {
    @LayoutRes public static final int LAYOUT_ID = R.layout.player;

    @BindView(R.id.player_name) TextView mName;
    @BindView(R.id.player_is_host) TextView mHost;
    @BindView(R.id.player_is_you) TextView mYou;


    public PlayerViewHolder(View view, IUserSelector selector) {
        super(view);
        ButterKnife.bind(this, view);
        itemView.setOnClickListener((v) ->{
            selector.playerClicked((String)itemView.getTag());
        });
    }

    public void setPlayerData(Player player, boolean selected) {
        mName.setText(player.username);
        @ColorRes int color = CreatureCreation.Colors.lookUp(player.color);
        mHost.setVisibility(player.isHost() ? View.VISIBLE : View.INVISIBLE);
        mYou.setVisibility(player.isYou() ? View.VISIBLE : View.INVISIBLE);
        mName.setBackgroundResource(selected ? R.drawable.border : 0);
        itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), color));
        itemView.setTag(player.color);
    }
}
