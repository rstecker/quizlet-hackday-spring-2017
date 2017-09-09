package studioes.arm.six.creatures;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by sithel on 9/8/17.
 */

public class BlobView extends View {
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

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(getResources().getColor(R.color.wl_blue));
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Path p = new Path();
        p.addCircle(0, 0, 20, Path.Direction.CCW);

        canvas.drawPath(p, mPaint);
    }
}
