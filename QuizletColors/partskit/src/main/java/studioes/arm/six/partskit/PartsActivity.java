package studioes.arm.six.partskit;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import javax.annotation.ParametersAreNonnullByDefault;

import static studioes.arm.six.partskit.ColorFragment.BRIGHTNESS_KEY;
import static studioes.arm.six.partskit.ColorFragment.COLOR_KEY;

/**
 * Created by sithel on 10/22/17.
 */
@ParametersAreNonnullByDefault
public class PartsActivity extends AppCompatActivity {
    @LayoutRes private static final int LAYOUT_ID = R.layout.activity_parts;
    public static final String TAG = PartsActivity.class.getSimpleName();


    ViewPager mViewPager;

    ColorFragment.Brightness mBrightness = ColorFragment.Brightness.LIGHT;
    ColorFragment.Color mColor = ColorFragment.Color.NEON;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Bundle bundle = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        if (bundle != null) {
            mBrightness = (ColorFragment.Brightness) bundle.get(BRIGHTNESS_KEY);
            mColor = (ColorFragment.Color) bundle.get(COLOR_KEY);
        }
        if (mColor == ColorFragment.Color.EARTH) {
            setTheme(isLight() ? R.style.Theme_QC_Light_Earth : R.style.Theme_QC_Dark_Earth);
        } else if (mColor == ColorFragment.Color.NEON) {
            setTheme(isLight() ? R.style.Theme_QC_Light_Neon : R.style.Theme_QC_Dark_Neon);
        } else if (mColor == ColorFragment.Color.PASTEL) {
            setTheme(isLight() ? R.style.Theme_QC_Light_Pastel : R.style.Theme_QC_Dark_Pastel);
        }

        setContentView(LAYOUT_ID);
        mViewPager = findViewById(R.id.pager);
        mViewPager.setAdapter(new Shark(getSupportFragmentManager()));
    }

    private boolean isLight() {
        return mBrightness == ColorFragment.Brightness.LIGHT;
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BRIGHTNESS_KEY, mBrightness);
        outState.putSerializable(COLOR_KEY, mColor);
    }

    public void refreshThings() {
        recreate();
    }

    static class Shark extends FragmentPagerAdapter {

        Shark(FragmentManager fm) {
            super(fm);
        }

        @Override public Fragment getItem(int position) {
            if (position == 1) {
                ColorFragment frag = new ColorFragment();
                Bundle args = new Bundle();
                frag.setArguments(args);
                return frag;
            } else if (position == 2) {
                return new CompassFragment();
            } else {
                return new GradingFragment();
            }
        }

        @Override public int getCount() {
            return 3;
        }
    }
}
