package sixarmstudios.quizletcolors.ui.setup;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import appstate.AppState;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import quizlet.QSet;
import sixarmstudios.quizletcolors.R;
import sixarmstudios.quizletcolors.StartActivity;
import sixarmstudios.quizletcolors.network.IModelRetrievalService;
import ui.SetSummary;
import ui.immutable.ImmSetSummary;
import ui.immutable.ImmutableImmSetSummary;
import viewmodel.TopLevelViewModel;

import static android.view.View.GONE;

/**
 * Created by rebeccastecker on 9/7/17.
 */

public class LookingForSetFragment extends LifecycleFragment {
    public static final String TAG = LookingForSetFragment.class.getSimpleName();
    @LayoutRes public static final int LAYOUT_ID = R.layout.fragment_looking_for_set;

    @BindView(R.id.username_text_field) EditText mUsernameField;
    @BindView(R.id.welcome_host_view) TextView mWelcomeTxtView;
    @BindView(R.id.set_list) RecyclerView mSetList;
    @BindView(R.id.refresh_overview) View mRefreshList;
    SetAdapter mAdapter;
    IModelRetrievalService mModelService;

    public static LookingForSetFragment newInstance() {
        LookingForSetFragment fragment = new LookingForSetFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(LAYOUT_ID, container, false);
        ButterKnife.bind(this, view);
        TopLevelViewModel viewModel = ViewModelProviders.of(this).get(TopLevelViewModel.class);
        viewModel.getAppState().observe(this, this::handleVMUpdates);
        viewModel.getSetSummaries().observe(this, this::handleSetUpdates);
        mAdapter = new SetAdapter();
        mSetList.setAdapter(mAdapter);
        mSetList.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        mModelService = ((StartActivity) context).getModelService();
    }

    @OnClick(R.id.refresh_overview)
    public void handleRefreshList() {
        mModelService.refreshSummary();
    }

    private void handleVMUpdates(List<AppState> states) {
        if (states == null || states.isEmpty()) {
            return;
        }
        AppState state = states.get(0);
        mWelcomeTxtView.setText(getResources().getString(R.string.host_welcome, state.qUsername));
        mUsernameField.setText(state.qUsername);
        mUsernameField.setHint(state.qUsername);
    }

    private void handleSetUpdates(List<SetSummary> summaries) {
        Log.i(TAG, "I see incoming summaries : " + summaries);
        if (summaries == null || summaries.isEmpty()) {
            return;
        }
        mAdapter.processLiveUpdate(summaries);
    }

    private static class SetAdapter extends RecyclerView.Adapter<SetViewHolder> {
        List<ImmSetSummary> summaries = new ArrayList<>();

        @Override
        public SetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(SetViewHolder.LAYOUT_ID, parent, false);
            return new SetViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SetViewHolder holder, int position) {
            if (position < 0 && position >= summaries.size()) {
                return;
            }
            holder.bind(summaries.get(position));
        }

        @Override public int getItemCount() {
            return summaries == null ? 0 : summaries.size();
        }

        void processLiveUpdate(@NonNull List<SetSummary> dbSummaries) {
            for (int i = 0; i < summaries.size() && i < dbSummaries.size(); ++i) {
                ImmSetSummary dbSummary = ImmSetSummary.from(dbSummaries.get(i));
                ImmSetSummary adapterSummary = summaries.get(i);
                if (!dbSummary.equals(adapterSummary)) {
                    summaries.set(i, dbSummary);
                    notifyItemChanged(i);
                }
            }
            if (summaries.size() < dbSummaries.size()) {
                int oldSize = summaries.size();
                for (int i = oldSize; i < dbSummaries.size(); ++i) {
                    summaries.add(ImmSetSummary.from(dbSummaries.get(i)));
                }
                notifyItemRangeInserted(oldSize, dbSummaries.size() - oldSize);
            } else if (summaries.size() > dbSummaries.size()) {
                int oldSize = summaries.size();
                for (int i = oldSize; i >= dbSummaries.size(); --i) {
                    summaries.remove(i);
                }
                notifyItemRangeRemoved(oldSize, oldSize - dbSummaries.size());
            }
            notifyDataSetChanged();
        }
    }

    static class SetViewHolder extends RecyclerView.ViewHolder {
        @LayoutRes public static final int LAYOUT_ID = R.layout.view_set_summary;

        @BindView(R.id.set_name) TextView mSetName;
        @BindView(R.id.set_desc) TextView mSetDesc;
        @BindView(R.id.creator_name) TextView mCreatorName;
        @BindView(R.id.term_count) TextView mTermCount;
        @BindView(R.id.synced) TextView mSynced;

        public SetViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(ImmSetSummary summary) {
            Resources r = itemView.getContext().getResources();
            mSetName.setText(summary.title());
            mSetDesc.setText(summary.description());
            mSetDesc.setVisibility(StringUtils.isEmpty(summary.description()) ? GONE : View.VISIBLE);
            mCreatorName.setText(summary.creatorUsername());
            mTermCount.setText(r.getString(R.string.term_count, summary.termCount()));
            Date d = new Date(summary.lastSynced());
            mSynced.setText(summary.lastSynced() == 0 ? r.getString(R.string.never_synced) : d.toString());
        }
    }
}
