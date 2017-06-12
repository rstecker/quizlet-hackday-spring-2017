package sixarmstudios.quizletcolors.ui.player;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import sixarmstudios.quizletcolors.R;
import sixarmstudios.quizletcolors.ui.board.IUserSelector;
import ui.Player;

/**
 * Created by rebeccastecker on 6/11/17.
 */

public class PlayerViewHolder extends RecyclerView.ViewHolder {
    @LayoutRes public static final int LAYOUT_ID = R.layout.player;

    @BindView(R.id.player_name) TextView mName;
    @BindView(R.id.player_color) TextView mColor;
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
        mColor.setText(player.color);
        mHost.setVisibility(player.isHost() ? View.VISIBLE : View.GONE);
        mYou.setVisibility(player.isYou() ? View.VISIBLE : View.GONE);
        itemView.setBackgroundColor(itemView.getContext().getColor(selected ? R.color.background_color_accent : R.color.background_color));
        itemView.setTag(player.color);
    }
}
