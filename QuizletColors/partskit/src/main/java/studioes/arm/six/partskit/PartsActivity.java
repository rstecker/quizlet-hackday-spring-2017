package studioes.arm.six.partskit;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioGroup;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Created by sithel on 10/22/17.
 */
@ParametersAreNonnullByDefault
public class PartsActivity extends AppCompatActivity {
    @LayoutRes private static final int LAYOUT_ID = R.layout.activity_parts;
    public static final String TAG = PartsActivity.class.getSimpleName();

    private static final String BRIGHTNESS_KEY = "BRIGHTNESS_KEY";
    private static final String COLOR_KEY = "COLOR_KEY";

    enum Brightness {
        LIGHT,
        DARK;
    }

    enum Color {
        NEON,
        PASTEL,
        EARTH;
    }

    Brightness mBrightness = Brightness.LIGHT;
    Color mColor = Color.NEON;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        if (bundle != null) {
            mBrightness = (Brightness) bundle.get(BRIGHTNESS_KEY);
            mColor = (Color) bundle.get(COLOR_KEY);
        }
        if (mColor == Color.EARTH) {
            setTheme(isLight() ? R.style.Theme_QC_Light_Earth : R.style.Theme_QC_Dark_Earth);
        } else if (mColor == Color.NEON) {
            setTheme(isLight() ? R.style.Theme_QC_Light_Neon : R.style.Theme_QC_Dark_Neon);
        } else if (mColor == Color.PASTEL) {
            setTheme(isLight() ? R.style.Theme_QC_Light_Pastel : R.style.Theme_QC_Dark_Pastel);
        }
        setContentView(LAYOUT_ID);
    }

    private boolean isLight() {
        return mBrightness == Brightness.LIGHT;
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BRIGHTNESS_KEY, mBrightness);
        outState.putSerializable(COLOR_KEY, mColor);
    }

    @Override protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        RadioGroup brightnessGroup = findViewById(R.id.theme_brithness);
        brightnessGroup.setOnCheckedChangeListener(this::onBrightnessChanged);
        ((RadioGroup) findViewById(R.id.theme_color)).setOnCheckedChangeListener(this::onBrightnessChanged);
    }

    public void onBrightnessChanged(RadioGroup radioGroup, int id) {
        if (id == R.id.radio_light) {
            mBrightness = Brightness.LIGHT;
            recreate();
        } else if (id == R.id.radio_dark) {
            mBrightness = Brightness.DARK;
            recreate();
        } else if (id == R.id.radio_earth) {
            mColor = Color.EARTH;
            recreate();
        } else if (id == R.id.radio_neon) {
            mColor = Color.NEON;
            recreate();
        } else if (id == R.id.radio_pastel) {
            mColor = Color.PASTEL;
            recreate();
        }
    }
}
