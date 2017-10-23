package studioes.arm.six.partskit;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;

/**
 * Created by sithel on 10/22/17.
 */

public class CompasRose extends FrameLayout {
    public static final String TAG = CompasRose.class.getSimpleName();
    @LayoutRes private static final int LAYOUT_ID = R.layout.compas_rose;

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

        int color2 = 0;
        Drawable line = null;
        try {
            color2 = a.getResourceId(R.styleable.CompasRose_lineColor, R.color.blue_neon);
            if (a.hasValue(R.styleable.CompasRose_lineType)) {
                line = a.getDrawable(R.styleable.CompasRose_lineType);
            }
        } finally {
            a.recycle();
        }
        inflate(context, LAYOUT_ID, this);
//        ((TextView) findViewById(R.id.test1)).setTextColor(getResources().getColor(color2));
        ColorStateList x = getResources().getColorStateList(color2);
//        findViewById(R.id.spot1).setBackgroundTintList(x);
        findViewById(R.id.compass_rose_lvl_1).setBackgroundTintList(x);
        findViewById(R.id.compass_rose_lvl_1).setBackground(line);
        findViewById(R.id.compass_rose_lvl_1).setRotation((float) (360 * Math.random()));
//        setClipToOutline(true);
//        setClipToPadding(false);

        setOutlineProvider(new ViewOutlineProvider() {
            @Override public void getOutline(View view, Outline outline) {
                Log.i(TAG, "Rebecca : getting that outline.. .view is "+view.getWidth()+", "+view.getHeight());
                int w = view.getWidth();
                int h = view.getHeight();
                if (w == 0 || h == 0) {
                    return;
                }
//                Path p = new Path();
//                p.addRect(new RectF(w/4,w/4,w*.75f,h*.75f), Path.Direction.CW);
//                p.moveTo(w/2, 0);
//                p.lineTo(w,75);
//                p.lineTo(w/2, h);
//                p.lineTo(0, 75);
//                p.close();
//                Log.i(TAG, "Rebecca ... is it convex? "+p.isConvex());
//                outline.setConvexPath(p);
                outline.setRect(0, 0, w/2, h/2);
//                outline.offset(50,50);
                Log.i(TAG, "Rebeca .... canClip? "+outline.canClip());
            }
        });


//        populateLayer(context, findViewById(R.id.compass_rose_lvl_1), line, x, 5);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        Log.i(TAG, "Rebecca : On finished inflate!");
    }


    private void populateLayer(Context context, FrameLayout view, Drawable drawable, ColorStateList colors, int lineCount) {
        for (int i = 0; i < lineCount; ++i) {
            FrameLayout.LayoutParams params=new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            View v = new FrameLayout(context);
            v.setLayoutParams(params);
            v.setBackground(drawable);
            v.setBackgroundTintList(colors);
            v.setRotation(360 / lineCount);
            view.addView(v, params);
            Log.i(TAG, "Rebecca : trying to add "+colors+" and "+drawable);
            view.setBackground(drawable);
            view.setBackgroundTintList(colors);
        }
    }


}
