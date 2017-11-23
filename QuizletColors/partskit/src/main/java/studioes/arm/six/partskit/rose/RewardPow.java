package studioes.arm.six.partskit.rose;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by rebeccastecker on 11/22/17.
 */

public class RewardPow extends View {
    private Paint mPaint;
    private Path mPath;
    private Drawable mDrawable;

    public RewardPow(Context context) {
        super(context);
    }

    public RewardPow(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RewardPow(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {

    }

    public void setDetails(@ColorRes int color, Drawable drawable) {
        mPaint = new Paint();
        mPaint.setColor(getResources().getColor(color));
        mPaint.setStyle(Paint.Style.FILL);
        mDrawable = drawable.mutate();
        mPath = new Path();
    }

    public void pow() {

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mDrawable.draw(canvas);
    }
}
