package sixarmstudios.quizletcolors.ui.scorePlayer;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import sixarmstudios.quizletcolors.R;

/**
 * Created by austinrobarts on 11/21/17.
 */

public class ScorePlayerViewHolder extends RecyclerView.ViewHolder {
    @LayoutRes public static final int LAYOUT_ID = R.layout.player_score;
    public static final String TAG = ScorePlayerViewHolder.class.getSimpleName();

    @BindView(R.id.score_player_name) TextView mPlayerName;
    @BindView(R.id.score_player_order) TextView mPlayerOrder;
    @BindView(R.id.score_player_points) TextView mPlayerPoints;

    public ScorePlayerViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void setPlayerInfo(String playerName, int order, int points) {
        mPlayerName.setText(playerName);
        mPlayerOrder.setText(Integer.toString(order));
        mPlayerPoints.setText(Integer.toString(points));
    }

}
