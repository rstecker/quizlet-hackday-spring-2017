package studioes.arm.six.partskit;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import studioes.arm.six.partskit.rose.RewardPow;
import studioes.arm.six.partskit.rose.TextRing;

/**
 * Created by sithel on 10/22/17.
 */

public class CompassRose extends FrameLayout {
    public static final String TAG = CompassRose.class.getSimpleName();
    @LayoutRes
    private static final int LAYOUT_ID = R.layout.compas_rose;


    public enum RoseColor {
        RED(R.attr.playerRed, "red"),
        GREEN(R.attr.playerGreen, "green"),
        BLUE(R.attr.playerBlue, "blue"),
        YELLOW(R.attr.playerYellow, "yellow"),
        ORANGE(R.attr.playerOrange, "orange"),
        VIOLET(R.attr.playerViolet, "violet"),;
        @AttrRes
        int colorAttr;
        String colorName;

        RoseColor(@AttrRes int colorAttr, String colorName) {
            this.colorAttr = colorAttr;
            this.colorName = colorName;
        }

        public String colorName() {
            return colorName;
        }

        public int colorAttr() {
            return colorAttr;
        }

        public static RoseColor findByColorName(String color) {
            for (RoseColor rc : values()) {
                if (rc.colorName.equals(color)) {
                    return rc;
                }
            }
            Log.e(TAG, "Failed to find a matching rose color to '" + color + "' - going with RED for now");
            return RED;
        }
    }

    public static @DrawableRes
    int getShapeBasedOnUsername(@NonNull String username) {
        return PLAYER_SHAPE_DRAWABLE_RES[username.hashCode() % PLAYER_SHAPE_DRAWABLE_RES.length];
    }

    public static final int[] PLAYER_SHAPE_DRAWABLE_RES = {
            R.drawable.line_beads, R.drawable.line_dimond, R.drawable.line_oval,
            R.drawable.line_rect, R.drawable.line_squiggle, R.drawable.line_triangle,
            R.drawable.line_triangle_down, R.drawable.line_triangle_up, R.drawable.line_zig_zag
    };

    private @ColorRes
    int mBaseLineColor;
    private Drawable mBaseDrawable;
    /**
     * Ranges from 0 - 1
     */
    private float mCurrentEnergy = 0;

    public CompassRose(Context context) {
        super(context);
    }

    public CompassRose(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CompassRose(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public CompassRose(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CompassRose,
                0, 0);

        try {
            mBaseLineColor = a.getResourceId(R.styleable.CompassRose_lineColor, R.color.blue_neon);
            if (a.hasValue(R.styleable.CompassRose_lineType)) {
                mBaseDrawable = a.getDrawable(R.styleable.CompassRose_lineType);
            }
        } finally {
            a.recycle();
        }

        inflate(context, LAYOUT_ID, this);
        mAni1 = new FlingAnimation(findViewById(R.id.compass_rose_lvl_1), DynamicAnimation.ROTATION)
                .setFriction(0.0001f).setStartVelocity(0).setStartValue(0);
        mAni2 = new FlingAnimation(findViewById(R.id.compass_rose_lvl_2), DynamicAnimation.ROTATION)
                .setFriction(0.0001f).setStartVelocity(0).setStartValue(0);
        mAni3 = new FlingAnimation(findViewById(R.id.compass_rose_lvl_3), DynamicAnimation.ROTATION)
                .setFriction(0.0001f).setStartVelocity(0).setStartValue(0);
        populateLayer(context, findViewById(R.id.compass_rose_lvl_1), 2, 15, 5);
        populateLayer(context, findViewById(R.id.compass_rose_lvl_2), 1, 6, 4);
        populateLayer(context, findViewById(R.id.compass_rose_lvl_3), 0, 10, 6);
    }

    public void disable() {
        this.setVisibility(INVISIBLE);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.background, typedValue, true);
        @ColorRes int color = typedValue.resourceId;
        mBaseLineColor = color;
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

    public void setPlayer(RoseColor roseColor, @DrawableRes int playerLineShapeRes) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(roseColor.colorAttr, typedValue, true);
        @ColorRes int color = typedValue.resourceId;
        mBaseLineColor = color;
        setLine(getContext().getDrawable(playerLineShapeRes));
        setTag(R.id.player_color_key, roseColor.colorName);
    }

    public void setPlayer(Player player) {
        setPlayer(player.getColor(), player.getLineShape());
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.gradeNoPlayerBg, typedValue, true);

        String innerText = player.isPlayer() ? "you" : player.getUsername();
        ((TextRing) findViewById(R.id.compass_rose_you_text_ring)).setRing(innerText, getContext().getColor(mBaseLineColor));
        findViewById(R.id.compass_rose_you_text_ring).setVisibility(VISIBLE);

        String outerText = (player.isHost() ? "host " : "") + "[" + player.getScore() + "]";
        ((TextRing) findViewById(R.id.compass_rose_host_text_ring)).setRing(outerText, typedValue.data);
        findViewById(R.id.compass_rose_host_text_ring).setVisibility(VISIBLE);

        ((RewardPow)findViewById(R.id.compass_rose_reward_pow)).setDetails(mBaseLineColor, mBaseDrawable);
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

    private FlingAnimation mAni1;
    private FlingAnimation mAni2;
    private FlingAnimation mAni3;

    public void boop() {
        shuffleStars(findViewById(R.id.compass_rose_lvl_1), 2, mAni1);
        shuffleStars(findViewById(R.id.compass_rose_lvl_2), 1, mAni2);
        shuffleStars(findViewById(R.id.compass_rose_lvl_3), 0, mAni3);
    }

    private @ColorInt
    int getColorVariant(int lvl) {
        @ColorInt int colorInt = ContextCompat.getColor(getContext(), mBaseLineColor);
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

    private void shuffleStars(FrameLayout frame, int lvl, FlingAnimation frameAnimation) {
        updateBasics(frame, lvl, frameAnimation);

//        Resources r = getResources();
//        float px25 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, r.getDisplayMetrics());

        Log.i(TAG, "Rebecca : [" + lvl + "] " + frame.getWidth() + ", " + frame.getHeight() + " --> " + frame.getChildAt(0).getWidth() + ", " + frame.getChildAt(0).getHeight());
        int r = frame.getHeight() / 5;
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

    private void updateBasics(FrameLayout frame, int lvl, FlingAnimation frameAnimation) {
        frameAnimation
                .setStartVelocity(mCurrentEnergy == 0 ? 0 : mCurrentEnergy * 150 + 150)
                .start();
        float newScale = (float) (lvl == 2 ? 1 : lvl == 1 ? 0.75 : 0.5);
        newScale = (float) (Math.max(0.02, mCurrentEnergy * 2) * newScale);
        new SpringAnimation(frame, DynamicAnimation.SCALE_X)
                .setMaxValue(Float.MAX_VALUE)
                .setStartVelocity(1f)
                .setSpring(new SpringForce()
                        .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
                        .setStiffness(SpringForce.STIFFNESS_LOW)
                        .setFinalPosition(newScale))
                .start();

        new SpringAnimation(frame, DynamicAnimation.SCALE_Y)
                .setMaxValue(Float.MAX_VALUE)
                .setStartVelocity(1f)
                .setSpring(new SpringForce()
                        .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
                        .setStiffness(SpringForce.STIFFNESS_LOW)
                        .setFinalPosition(newScale))
                .start();
    }

    /**
     * @param i how PUMPED are you??  0 - 1, 0 being lame and zZZzZZz and 1 being YEAH! (and 1.5 being on speed)
     */
    public void setEnergy(float i) {
        mCurrentEnergy = i;

        updateBasics(findViewById(R.id.compass_rose_lvl_1), 2, mAni1);
        updateBasics(findViewById(R.id.compass_rose_lvl_2), 1, mAni2);
        updateBasics(findViewById(R.id.compass_rose_lvl_3), 0, mAni3);
        this.invalidate();
    }

    public Drawable getPlayerShape() {
        return mBaseDrawable;
    }

    public @ColorRes
    int getColorRes() {
        return mBaseLineColor;
    }

    public String getPlayerColor() {
        return (String) getTag(R.id.player_color_key);
    }

    public void reward() {
        ((RewardPow)findViewById(R.id.compass_rose_reward_pow)).pow();
    }
}
