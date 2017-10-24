package studioes.arm.six.partskit;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by sithel on 10/22/17.
 */

public class CompasRose extends FrameLayout {
    public static final String TAG = CompasRose.class.getSimpleName();
    @LayoutRes private static final int LAYOUT_ID = R.layout.compas_rose;


    @ColorRes int mBaseLineColor;
    Drawable mBaseDrawable;

    public CompasRose(Context context) {
        super(context);
    }

    public CompasRose(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CompasRose(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public CompasRose(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CompasRose,
                0, 0);

        try {
            mBaseLineColor = a.getResourceId(R.styleable.CompasRose_lineColor, R.color.blue_neon);
            if (a.hasValue(R.styleable.CompasRose_lineType)) {
                mBaseDrawable = a.getDrawable(R.styleable.CompasRose_lineType);
            }
        } finally {
            a.recycle();
        }

        inflate(context, LAYOUT_ID, this);

        populateLayer(context, findViewById(R.id.compass_rose_lvl_1), 2, 15, 5);
        populateLayer(context, findViewById(R.id.compass_rose_lvl_2), 1, 6, 4);
        populateLayer(context, findViewById(R.id.compass_rose_lvl_3), 0, 10, 6);
    }

    private Drawable newDrawable() {
        return mBaseDrawable.getConstantState().newDrawable().mutate();
    }

    private void populateLayer(Context context, FrameLayout view, int lvl, int lineCount, int starCount) {
        Drawable drawable = newDrawable();
        drawable.setTint(getColorVariant(lvl));
        int r = 20;
        for (int i = 0; i < Math.max(lineCount, starCount); ++i) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
            View v = new FrameLayout(context);
            v.setLayoutParams(params);
            int deg = 360 / starCount * (i % starCount);
            double rad = Math.toRadians(deg);
            v.setTranslationX((float) Math.cos(rad) * r);
            v.setTranslationY((float) Math.sin(rad) * r);
            v.setScaleX(0.15f);
            v.setScaleY(0.5f);
            v.setBackground(drawable);
            v.setRotation(deg + 90);
            view.addView(v, params);
        }
    }

    public void setLine(Drawable drawable) {
        mBaseDrawable = drawable.mutate();
        setDrawable(findViewById(R.id.compass_rose_lvl_1), 2);
        setDrawable(findViewById(R.id.compass_rose_lvl_2), 1);
        setDrawable(findViewById(R.id.compass_rose_lvl_3), 0);

    }

    /**
     * @param lvl the amount of drift. 0 means closed to original. Goes up from there (0-10)
     */
    private void setDrawable(FrameLayout frame, int lvl) {
        Drawable d = newDrawable();
        d.setTint(getColorVariant(lvl));
        int max = frame.getChildCount();
        for (int i = 0; i < max; ++i) {
            frame.getChildAt(i).setBackground(d);
        }
    }

    public void boop() {
        shuffleStars(findViewById(R.id.compass_rose_lvl_1), 2);
        shuffleStars(findViewById(R.id.compass_rose_lvl_2), 1);
        shuffleStars(findViewById(R.id.compass_rose_lvl_3), 0);
    }

    private @ColorInt int getColorVariant(int lvl) {
        @ColorInt int colorInt = getResources().getColor(mBaseLineColor);
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(colorInt, hsl);
        double skew = 0.01 * lvl;
//        Log.i(TAG, "Rebecca, transitioning color. Was " + hsl[0]+", "+hsl[1]+", "+hsl[2]);
        hsl[2] = (float) Math.min(1, Math.max(0, hsl[2] + skew));
        hsl[1] = (float) Math.min(1, Math.max(0, hsl[1] - Math.random() / 4));
        colorInt = ColorUtils.HSLToColor(hsl);
//        Log.i(TAG, "Rebecca,    ... now is w/ skew ("+skew+") -> " + hsl[0]+", "+hsl[1]+", "+hsl[2]);
        colorInt = colorInt & 0x3FFFFFFF;
        return colorInt;
    }

    private void shuffleStars(FrameLayout frame, int lvl) {
        frame.animate()
                .rotationBy((float) (360 + 360*Math.random()))
                .setDuration((long) (2*1000+Math.random()*2*1000))
                .start();

//        Resources r = getResources();
//        float px25 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, r.getDisplayMetrics());

        Log.i(TAG, "Rebecca : [" + lvl + "] " + frame.getWidth() + ", " + frame.getHeight() + " --> " + frame.getChildAt(0).getWidth() + ", " + frame.getChildAt(0).getHeight());
        int r = frame.getHeight() / 5;//int) (Math.random() * 30 + 5);
        int max = frame.getChildCount();
        int star = Math.min(max, Math.max(1, (int) (Math.random() * max / 2 + max / 2)));

        int startOffset = 0;//(int) (Math.random() * max);
        for (int i = 0; i < max; ++i) {
            View v = frame.getChildAt((i + startOffset) % max);
            int deg = 360 / star * (i % star);
            double rad = Math.toRadians(deg);

            float newX = (float) (Math.cos(rad) * r);
            float newY = (float) (Math.sin(rad) * r);
            float newRot = deg + 90;

            new SpringAnimation(v, DynamicAnimation.TRANSLATION_X)
                    .setStartVelocity(200)
                    .setMaxValue(Float.MAX_VALUE)
                    .setSpring(new SpringForce()
                            .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
                            .setStiffness(SpringForce.STIFFNESS_LOW)
                            .setFinalPosition(newX))
                    .start();

            new SpringAnimation(v, DynamicAnimation.TRANSLATION_Y)
                    .setMaxValue(Float.MAX_VALUE)
                    .setStartVelocity(200)
                    .setSpring(new SpringForce()
                            .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
                            .setStiffness(SpringForce.STIFFNESS_LOW)
                            .setFinalPosition(newY))
                    .start();

            new SpringAnimation(v, DynamicAnimation.ROTATION)
                    .setMaxValue(Float.MAX_VALUE)
                    .setStartVelocity(200)
                    .setSpring(new SpringForce()
                            .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
                            .setStiffness(SpringForce.STIFFNESS_LOW)
                            .setFinalPosition(newRot))
                    .start();

            new SpringAnimation(v, DynamicAnimation.ALPHA)
                    .setMaxValue(Float.MAX_VALUE)
                    .setStartVelocity(1f)
                    .setSpring(new SpringForce()
                            .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
                            .setStiffness(SpringForce.STIFFNESS_LOW)
                            .setFinalPosition((float) Math.random() + 0.02f))
                    .start();

        }

    }


}
