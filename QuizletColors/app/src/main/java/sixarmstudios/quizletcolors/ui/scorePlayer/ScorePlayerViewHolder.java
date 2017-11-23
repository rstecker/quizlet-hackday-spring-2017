package sixarmstudios.quizletcolors.ui.scorePlayer;

import android.content.res.Resources;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import sixarmstudios.quizletcolors.R;
import studioes.arm.six.partskit.CompassRose;

/**
 * Created by austinrobarts on 11/21/17.
 */

public class ScorePlayerViewHolder extends RecyclerView.ViewHolder {
    @LayoutRes public static final int LAYOUT_ID = R.layout.player_score;
    public static final String TAG = ScorePlayerViewHolder.class.getSimpleName();

    @BindView(R.id.score_player_card) CardView mPlayerCard;
    @BindView(R.id.score_player_name) TextView mPlayerName;
    @BindView(R.id.score_player_order) TextView mPlayerOrder;
    @BindView(R.id.score_player_points) TextView mPlayerPoints;

    public ScorePlayerViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void setPlayerInfo(String playerName, int order, int points, CompassRose.RoseColor color) {
        mPlayerName.setText(playerName);
        mPlayerOrder.setText(Integer.toString(order) + ")");
        mPlayerPoints.setText(Integer.toString(points)+ " points");
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = itemView.getContext().getTheme();
        theme.resolveAttribute(color.colorAttr(), typedValue, true);
        mPlayerCard.setCardBackgroundColor(typedValue.data);
    }

}
