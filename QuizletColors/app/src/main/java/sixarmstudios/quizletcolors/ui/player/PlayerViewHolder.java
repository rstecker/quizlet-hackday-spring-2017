package sixarmstudios.quizletcolors.ui.player;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import sixarmstudios.quizletcolors.R;
import sixarmstudios.quizletcolors.logic.player.CreatureCreation;
import sixarmstudios.quizletcolors.ui.board.IUserSelector;
import studioes.arm.six.partskit.CompasRose;
import ui.Player;

import static studioes.arm.six.partskit.CompasRose.PLAYER_SHAPE_DRAWABLE_RES;

/**
 * Created by rebeccastecker on 6/11/17.
 */

public class PlayerViewHolder extends RecyclerView.ViewHolder {
    @LayoutRes public static final int LAYOUT_ID = R.layout.player;
    public static final String TAG = PlayerViewHolder.class.getSimpleName();


    @BindView(R.id.player_name) TextView mName;
    @BindView(R.id.player_is_host) TextView mHost;
    @BindView(R.id.player_is_you) TextView mYou;
    @BindView(R.id.player_icon) CompasRose mPlayerIcon;


    public PlayerViewHolder(View view, IUserSelector selector) {
        super(view);
        ButterKnife.bind(this, view);
        itemView.setOnClickListener((v) -> {
            selector.playerClicked((String) itemView.getTag());
        });
    }

    public void setPlayerData(Player player, boolean selected) {
        final String username = player.username;
        mName.setText(player.username);
        @ColorRes int color = CreatureCreation.Colors.lookUp(player.color);
        mHost.setVisibility(player.isHost() ? View.VISIBLE : View.INVISIBLE);
        mYou.setVisibility(player.isYou() ? View.VISIBLE : View.INVISIBLE);
        mName.setBackgroundResource(selected ? R.drawable.border : 0);
//        itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), color));
        itemView.setTag(player.color);
        mPlayerIcon.setPlayer(new studioes.arm.six.partskit.Player(
                player.username,
                randomPlayerColor(),
                0,
                player.isHost(),
                player.isYou(),
                getRandomShape()
        ));
    }


    private CompasRose.RoseColor randomPlayerColor() {
        int i = (int) Math.max(0, Math.min(CompasRose.RoseColor.values().length, Math.random() * CompasRose.RoseColor.values().length));
        return CompasRose.RoseColor.values()[i];
    }

    private @DrawableRes
    int getRandomShape() {
        int i = (int) Math.max(0, Math.min(PLAYER_SHAPE_DRAWABLE_RES.length, Math.random() * PLAYER_SHAPE_DRAWABLE_RES.length));
        return PLAYER_SHAPE_DRAWABLE_RES[i];
    }

    private Disposable mAnimation;
}
