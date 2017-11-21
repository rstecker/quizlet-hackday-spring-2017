package studioes.arm.six.partskit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by rebeccastecker on 11/20/17.
 */

public class TextRing extends View {
    public static final String TAG = TextRing.class.getSimpleName();

    private Paint mPaint;
    private Path mPath;
    private String mMsg;
    private float mRadius;

    public TextRing(Context context) {
        super(context);
        init();
    }

    public TextRing(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextRing(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TextRing(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPath = new Path();
        setBackgroundResource(R.color.transparent);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mPath.reset();
        mPath.moveTo((top - bottom) / 2f, (right - left) / 2f);
        float w = right - left;
        float h = bottom - top;
        mRadius = Math.min(w / 3f, h / 3f);
        Log.i(TAG, "We see " + w + " , " + h);
        mPath.addCircle(w / 2f, h / 2f, mRadius, Path.Direction.CW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(mPath, mPaint);

        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setTextSize(40);

        //drawTextOnPath(text, path, hOffset, vOffset, paint)
        if (mMsg != null) {
            float offset = (float) Math.PI * 2f * mRadius / 20;
            canvas.drawTextOnPath(mMsg, mPath, offset, -20, mPaint);
            float fourth = (float) Math.PI * 2f * mRadius / 4;
            canvas.drawTextOnPath(mMsg, mPath, fourth + offset, -20, mPaint);
            canvas.drawTextOnPath(mMsg, mPath, fourth * 2 + offset, -20, mPaint);
            canvas.drawTextOnPath(mMsg, mPath, fourth * 3 + offset, -20, mPaint);
        }
    }

    public void setRing(String msg, @ColorInt int color) {
        mMsg = msg;
        Log.i(TAG, "Setting the paint info");
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(color);
        mPaint.setStrokeWidth(3);
        invalidate();
    }
}
