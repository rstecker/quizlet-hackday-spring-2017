package studioes.arm.six.partskit.rose;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.concurrent.ThreadLocalRandom;

import studioes.arm.six.partskit.R;

/**
 * Created by rebeccastecker on 11/22/17.
 */

public class RewardPow extends View {
    public static final String TAG = RewardPow.class.getSimpleName();
    private Paint mPaint;
    private Path mPath;
    private Drawable mDrawable;
    private float mFloat;
    private int mPowCount;

    public RewardPow(Context context) {
        super(context);
        init();
    }

    public RewardPow(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public RewardPow(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundResource(R.color.transparent);
    }

    public void setDetails(@ColorRes int color, Drawable drawable) {
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(R.color.red));
        mPaint.setStyle(Paint.Style.FILL);
        mDrawable = drawable.mutate();
        mDrawable.setTint(getResources().getColor(R.color.green));
        mPath = new Path();
    }

    public void pow(@ColorRes int color, Drawable drawable) {
        Log.i(TAG, "I see a POW! "+color+" : "+drawable);
        mDrawable = drawable.mutate();
        mDrawable.setTint(getResources().getColor(color));
        mPowCount = ThreadLocalRandom.current().nextInt(6, 20);
        this.animate()
                .setUpdateListener(animation -> {
                    mFloat = animation.getAnimatedFraction();
                    invalidate();
                })
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(1500)
                .start();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mPath == null || mDrawable == null || mPowCount <= 0) {
            return;
        }
        mPath.reset();
        float w = (right - left);
        float h = (bottom - top);
        mPath.addCircle(w / 2f, h / 2f, w / 3, Path.Direction.CW);
        int sizeW = (int) w / 10;
        int sizeH = (int) h / 8;
        int posX = (int) w / 3;
        int posY = (int) h / 3;
        mDrawable.setBounds(posX, posY, posX + sizeW, posY + sizeH);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawable == null || mPaint == null || mPath == null || mPowCount <= 0) {
            return;
        }
//        canvas.drawPath(mPath, mPaint);
        float centerX = getWidth() / 2f;
        float centerY = getWidth() / 2f;

        int sizeW = getWidth() / 26;
        int sizeH = getHeight() / 8;
        int posX = getWidth() / 2 - sizeW / 2;
        int posY = (int) (getHeight() * 2 / 3f * (1 - mFloat) - (getHeight() / 3f));

        mDrawable.setBounds(posX, posY, posX + sizeW, posY + sizeH);
        mDrawable.setAlpha((int) (255 * (1 - mFloat)));
        float rotationBy = 350 / mPowCount;
        mDrawable.draw(canvas);
        for (int i = 0; i < mPowCount; ++i) {
            canvas.rotate(rotationBy, centerX, centerY);
            mDrawable.draw(canvas);
        }
    }
}
