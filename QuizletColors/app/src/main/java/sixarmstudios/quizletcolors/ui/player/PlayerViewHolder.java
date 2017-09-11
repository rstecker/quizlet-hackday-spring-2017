package sixarmstudios.quizletcolors.ui.player;

import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import sixarmstudios.quizletcolors.R;
import sixarmstudios.quizletcolors.logic.player.CreatureCreation;
import sixarmstudios.quizletcolors.ui.board.IUserSelector;
import studioes.arm.six.creatures.BlobView;
import ui.Player;

/**
 * Created by rebeccastecker on 6/11/17.
 */

public class PlayerViewHolder extends RecyclerView.ViewHolder {
    @LayoutRes public static final int LAYOUT_ID = R.layout.player;
    public static final String TAG = PlayerViewHolder.class.getSimpleName();


    @BindView(R.id.player_name) TextView mName;
    @BindView(R.id.player_is_host) TextView mHost;
    @BindView(R.id.player_is_you) TextView mYou;
    @BindView(R.id.player_creature) BlobView mCreature;


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
        mCreature.setDetails(player.username, color);

        // Create a low stiffness, low bounce spring at position 0.
        SpringForce spring = new SpringForce(0)
                .setDampingRatio(mCreature.getDampingRatio())
                .setStiffness(mCreature.getStiffness());

        final SpringAnimation anim = new SpringAnimation(mCreature, DynamicAnimation.TRANSLATION_Y)
                .setMinValue(-500).setSpring(spring).setStartValue(mCreature.getStartY());
        anim.start();

        mAnimation = Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .filter((t) -> Math.random() > mCreature.getTwitchynessThreshold())
                .subscribe((t) -> {
                    new SpringAnimation(mCreature, DynamicAnimation.TRANSLATION_Y)
                            .setMinValue(-500)
                            .setSpring(spring)
                            .setStartVelocity(mCreature.getStartVelocity())
                            .start();
                })
        ;
//        mCreature.invalidate();
//        itemView.requestLayout();
    }

    private Disposable mAnimation;
}
