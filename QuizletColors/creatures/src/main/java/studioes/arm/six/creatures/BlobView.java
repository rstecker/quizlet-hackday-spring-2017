package studioes.arm.six.creatures;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.animation.SpringForce;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by sithel on 9/8/17.
 */

public class BlobView extends View {
    String mUsername = "rebtest";
    public static final String TAG = BlobView.class.getSimpleName();

    public BlobView(Context context) {
        super(context);
    }

    public BlobView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BlobView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BlobView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    Paint mPaint;
    Path mPath = new Path();

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getResources().getColor(R.color.wl_blue));
        setUpPath();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSizeAndState(minw, widthMeasureSpec, 1);

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        int minh = MeasureSpec.getSize(w) + getPaddingBottom() + getPaddingTop();
        int h = resolveSizeAndState(MeasureSpec.getSize(w), heightMeasureSpec, 0);

        Log.i(TAG, " pre : "+getWidth()+", "+getHeight()+" :: "+w+":"+h);
        setMeasuredDimension(w, h);
        Log.i(TAG, " post : "+getWidth()+", "+getHeight()+" :: "+w+":"+h);
        setUpPath();
    }

    public void setDetails(String username, @ColorRes int colorRes) {
        mPaint.setColor(getResources().getColor(colorRes));
        mUsername = username;
        setUpPath();
        invalidate();
    }

    private void setUpPath() {
        mPath.reset();
        int hash = mUsername.hashCode();
        int bodyType = (hash >> 5 & 0b11) % 3;
        float scale = (hash & 0xFF) * 1f / 0xFF;
        float torsoCenterX = getWidth() * 1.0f / 2.0f;
        float torsoCenterY = getHeight() * 1.0f / 2.0f;
        float radius = Math.min(torsoCenterX, torsoCenterY) * 0.95f * scale;
        Log.i(TAG, "  > Username is : " + mUsername);
        Log.i(TAG, "  >> Hash is : " + hash);
        Log.i(TAG, "  >> Body type is : " + bodyType);
        Log.i(TAG, "  >> scale : " + scale);
        Log.i(TAG, "  >> center : " + torsoCenterX + ", " + torsoCenterY);
        Log.i(TAG, "  >> radius : " + radius);

        if (bodyType == 0) {    // square
            mPath.addRect(torsoCenterX - radius, torsoCenterY - radius, torsoCenterX + radius, torsoCenterY + radius, Path.Direction.CW);
        } else if (bodyType == 1) { // circle
            mPath.addCircle(torsoCenterX, torsoCenterY, radius, Path.Direction.CW);
        } else if (bodyType == 2) { // triangle
            mPath.moveTo(torsoCenterX, torsoCenterY - radius);
            mPath.lineTo(torsoCenterX + radius, torsoCenterY + radius / 2f);
            mPath.lineTo(torsoCenterX - radius, torsoCenterY + radius / 2f);
            mPath.close();
        }

    }


    public float getDampingRatio() {
        int hash = mUsername.hashCode();
        float scale = (hash >> 6 & 0xFF) * 1f / 0xFF;
        return SpringForce.DAMPING_RATIO_LOW_BOUNCY * scale;
    }

    public float getStiffness() {
        int hash = mUsername.hashCode();
        float scale = (hash >> 5 & 0xFFF) * 1f / 0xFFF;
        return SpringForce.STIFFNESS_LOW / scale;
    }

    public float getStartY() {
        int hash = mUsername.hashCode();
        float scale = (hash >> 3 & 0xFF) * 1f / 0xFF;
        return -200 * scale;
    }

    /**
     * @return if a random number (0-1) is greater than this value, we WILL witch (checked every second)
     */
    public double getTwitchynessThreshold() {
        int hash = mUsername.hashCode();
        return Math.max((hash >> 6 & 0xFFFF) * 1f / 0xFFFF, 0.05);
    }

    public float getStartVelocity() {
        int hash = mUsername.hashCode();
        return Math.min((float) ((hash >> 4 & 0xFFF) * (Math.random() * -2f)), -20);
    }
}
