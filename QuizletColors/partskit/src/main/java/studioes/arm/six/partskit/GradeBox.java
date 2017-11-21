package studioes.arm.six.partskit;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by sithel on 10/26/17.
 */

public class GradeBox extends LinearLayout {
    public static final String TAG = GradeBox.class.getSimpleName();
    @LayoutRes
    private static final int LAYOUT_ID = R.layout.view_grade_box;

    public GradeBox(@NonNull Context context) {
        super(context);
    }

    public GradeBox(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public GradeBox(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public GradeBox(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CompasRose,
                0, 0);

        try {
//            mBaseLineColor = a.getResourceId(R.styleable.CompasRose_lineColor, R.color.blue_neon);
//            if (a.hasValue(R.styleable.CompasRose_lineType)) {
//                mBaseDrawable = a.getDrawable(R.styleable.CompasRose_lineType);
//            }
        } finally {
            a.recycle();
        }

        inflate(context, LAYOUT_ID, this);
        setBackgroundResource(0);
    }

    public void lockIt() {
        this.setClickable(false);
        animate().cancel();
        setScaleX(0);
        setScaleY(0);
    }

    public void popIt() {
        this.setClickable(true);
        SpringForce spring = new SpringForce(1)
                .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
                .setStiffness(SpringForce.STIFFNESS_LOW);
        new SpringAnimation(this, DynamicAnimation.SCALE_X)
                .setMinValue(0)
                .setSpring(spring)
                .start()
        ;
        new SpringAnimation(this, DynamicAnimation.SCALE_Y)
                .setMinValue(0)
                .setSpring(spring)
                .start()
        ;
    }
    public void populateWrongAnswer(@NonNull String top, @Nullable String topColor, @NonNull String wrong, @Nullable String wrongColor, @NonNull String correct, @Nullable String correctColor) {
        setBoxDetails(R.id.boxCenter, top, topColor);
        setBoxDetails(R.id.boxRight, correct, correctColor);
        setBoxDetails(R.id.boxLeft, wrong, wrongColor);
    }

    private void setBoxDetails(@IdRes int id, @NonNull String text, @Nullable String color) {
        TextView view = findViewById(R.id.boxCenter);
        view.setText(text);
        int colorAttr = R.attr.gradeNoPlayerBg;
        if (color != null) {
            colorAttr = CompasRose.RoseColor.findByColorName(color).colorAttr;
        }
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(colorAttr, typedValue, true);
        int[][] states = new int[][]{new int[]{android.R.attr.state_activated}, new int[]{-android.R.attr.state_activated}};
        int[] colors = new int[]{typedValue.data, typedValue.data};
        ColorStateList list = new ColorStateList(states, colors);
        view.setBackgroundTintList(list);
    }
}
