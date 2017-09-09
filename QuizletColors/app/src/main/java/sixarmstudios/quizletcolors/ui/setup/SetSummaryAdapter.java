package sixarmstudios.quizletcolors.ui.setup;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import ui.SetSummary;
import ui.immutable.ImmSetSummary;
import ui.immutable.ImmutableImmSetSummary;

/**
 * Created by rebeccastecker on 9/8/17.
 */
class SetSummaryAdapter extends RecyclerView.Adapter<SetViewHolder> implements SetViewHolder.Callback {
    private List<ImmSetSummary> summaries = new ArrayList<>();

    interface Callback {
        Completable sync(long setId);

        void start(long setId);
    }

    private Callback mCallback;

    public SetSummaryAdapter(Callback callback) {
        super();
        mCallback = callback;
    }

    @Override
    public SetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(SetViewHolder.LAYOUT_ID, parent, false);
        return new SetViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(SetViewHolder holder, int position) {
        ImmSetSummary summary = getSummaryAtAdapterPosition(position);
        if (summary == null) {
            return;
        }
        holder.bind(summary);
    }

    @Override public int getItemCount() {
        return summaries == null ? 0 : summaries.size();
    }

    private @Nullable ImmSetSummary getSummaryAtAdapterPosition(int adapterPosition) {
        if (adapterPosition < 0 && adapterPosition >= summaries.size()) {
            return null;
        }
        return summaries.get(adapterPosition);
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

    @Override public void sync(int position) {
        ImmSetSummary summary = getSummaryAtAdapterPosition(position);
        if (summary == null) {
            return;
        }
        summaries.set(position, new ImmutableImmSetSummary.Builder()
                .from(summary)
                .syncing(true)
                .build());
        mCallback.sync(summary.id()).subscribe(() -> {
            ImmSetSummary updatedSummary = getSummaryAtAdapterPosition(position);
            if (updatedSummary == null) {
                return;
            }
            summaries.set(position, new ImmutableImmSetSummary.Builder()
                    .from(updatedSummary)
                    .syncing(false)
                    .build());
        });
    }

    @Override public void start(int position) {
        ImmSetSummary summary = getSummaryAtAdapterPosition(position);
        if (summary == null) {
            return;
        }
        mCallback.start(summary.id());
    }
}
