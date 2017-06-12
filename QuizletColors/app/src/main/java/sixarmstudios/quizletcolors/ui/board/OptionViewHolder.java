package sixarmstudios.quizletcolors.ui.board;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import sixarmstudios.quizletcolors.R;

/**
 * Created by rebeccastecker on 6/11/17.
 */

public class OptionViewHolder extends RecyclerView.ViewHolder {
    @LayoutRes public static final int LAYOUT_ID = R.layout.option;

    @BindView(R.id.option_text) TextView mOption;

    public OptionViewHolder(View itemView, IOptionSelector selector) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener((v) -> {
            selector.optionClicked((String)itemView.getTag());
        });
    }

    public void setOptionText(String option, boolean selected) {
        mOption.setText(option);
        itemView.setTag(option);
        itemView.setBackgroundColor(itemView.getContext().getColor(selected ? R.color.background_color_active : R.color.background_color_accent));
    }
}
