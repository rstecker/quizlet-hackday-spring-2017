package sixarmstudios.quizletcolors.ui.setup;

import android.content.res.Resources;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.telecom.Call;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import sixarmstudios.quizletcolors.R;
import ui.immutable.ImmSetSummary;

import static android.view.View.GONE;

/**
 * Created by rebeccastecker on 9/8/17.
 */
class SetViewHolder extends RecyclerView.ViewHolder {
    @LayoutRes public static final int LAYOUT_ID = R.layout.view_set_summary;
    interface Callback {
        void sync(int position);
        void start(int position);
    }

    @BindView(R.id.set_name) TextView mSetName;
    @BindView(R.id.set_desc) TextView mSetDesc;
    @BindView(R.id.creator_name) TextView mCreatorName;
    @BindView(R.id.term_count) TextView mTermCount;
    @BindView(R.id.summary_synced_info) TextView mSyncedInfo;
    @BindView(R.id.summary_sync_button) ImageView mSyncBtn;
    @BindView(R.id.summary_start_game_button) ImageView mStartBtn;

    public SetViewHolder(View itemView, Callback callback) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        mSyncBtn.setOnClickListener((v) -> callback.sync(getAdapterPosition()));
        mStartBtn.setOnClickListener((v) -> callback.start(getAdapterPosition()));
    }

    public void bind(ImmSetSummary summary) {
        Resources r = itemView.getContext().getResources();
        mSetName.setText(summary.title());
        mSetDesc.setText(summary.description());
        mSetDesc.setVisibility(StringUtils.isEmpty(summary.description()) ? GONE : View.VISIBLE);
        mCreatorName.setText(summary.creatorUsername());
        mTermCount.setText(r.getString(R.string.term_count, summary.termCount()));
        setUpSyncInfo(summary.lastSynced());
    }
    private void setUpSyncInfo(long lastSynced) {
        Resources r = itemView.getContext().getResources();
        Date d = new Date(lastSynced);
        boolean hasSynced = lastSynced != 0;
        mSyncedInfo.setText(hasSynced ?  new SimpleDateFormat("MM/dd/yy").format(d).toString() : r.getString(R.string.never_synced));
        mSyncBtn.setImageResource(hasSynced ? R.drawable.ic_sync : R.drawable.ic_download);
        mStartBtn.setVisibility(hasSynced ? View.VISIBLE : View.INVISIBLE);
    }
}
