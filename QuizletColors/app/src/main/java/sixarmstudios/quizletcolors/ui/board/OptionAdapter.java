package sixarmstudios.quizletcolors.ui.board;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ui.Option;

/**
 * Created by rebeccastecker on 6/11/17.
 */

public class OptionAdapter extends RecyclerView.Adapter<OptionViewHolder> {
    public static final String TAG = OptionAdapter.class.getSimpleName();
    private List<Option> mOptions;
    private IOptionSelector mSelector;
    private String mSelectedOption;

    public OptionAdapter(IOptionSelector selector) {
        mOptions = new ArrayList<>();
        mSelector = selector;
    }

    public void setOptions(List<Option> options) {
        if (options == null || options.size() < mOptions.size()) {
            return;
        }
        mOptions = options;
        notifyDataSetChanged(); // TODO : make this smarter
    }

    @Override public OptionViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(OptionViewHolder.LAYOUT_ID, viewGroup, false);
        return new OptionViewHolder(view, mSelector);
    }

    @Override public void onBindViewHolder(OptionViewHolder optionViewHolder, int i) {
        if (isInvalidIndex(i)) {
            return;
        }
        String option = mOptions.get(i).option;
        optionViewHolder.setOptionText(option, option.equals(mSelectedOption));

    }

    @Override public int getItemCount() {
        return mOptions.size();
    }

    private boolean isInvalidIndex(int i) {
        return i < 0 && i >= mOptions.size();
    }

    public void setSelectedOption(String newSelectedOption) {
        String oldSelectedOption = mSelectedOption;
        mSelectedOption = newSelectedOption;
        int oldIndex = -1;
        int newIndex = -1;
        for (int i = 0; i < mOptions.size(); ++i) {
            if (mOptions.get(i).option == null) {
                continue;
            }
            if (mOptions.get(i).option.equals(oldSelectedOption)) {
                oldIndex = i;
            }
            if (mOptions.get(i).option.equals(newSelectedOption)) {
                newIndex = i;
            }
        }
        notifyItemChanged(oldIndex);
        notifyItemChanged(newIndex);
    }
}
