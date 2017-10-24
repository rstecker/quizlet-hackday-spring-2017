package studioes.arm.six.partskit;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

/**
 * Created by sithel on 10/23/17.
 */

public class ColorFragment extends Fragment {
    @LayoutRes private static final int LAYOUT_ID = R.layout.fragment_colors;

    static final String BRIGHTNESS_KEY = "BRIGHTNESS_KEY";
    static final String COLOR_KEY = "COLOR_KEY";

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

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = savedInstanceState != null ? savedInstanceState : getArguments();
        if (bundle != null) {
            mBrightness = (Brightness) bundle.get(BRIGHTNESS_KEY);
            mColor = (Color) bundle.get(COLOR_KEY);
        }
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);


    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(LAYOUT_ID, container, false);
        return v;
    }

    @Override public void onResume() {
        super.onResume();
        RadioGroup brightnessGroup = getView().findViewById(R.id.theme_brithness);
        brightnessGroup.setOnCheckedChangeListener(this::onBrightnessChanged);
        ((RadioGroup) getView().findViewById(R.id.theme_color)).setOnCheckedChangeListener(this::onBrightnessChanged);
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BRIGHTNESS_KEY, mBrightness);
        outState.putSerializable(COLOR_KEY, mColor);
    }


    public void onBrightnessChanged(RadioGroup r, int id) {
        if (id == R.id.radio_light) {
            mBrightness = Brightness.LIGHT;
            updateTheme();
        } else if (id == R.id.radio_dark) {
            mBrightness = Brightness.DARK;
            updateTheme();
        } else if (id == R.id.radio_earth) {
            mColor = Color.EARTH;
            updateTheme();
        } else if (id == R.id.radio_neon) {
            mColor = Color.NEON;
            updateTheme();
        } else if (id == R.id.radio_pastel) {
            mColor = Color.PASTEL;
            updateTheme();
        }
    }

    private void updateTheme() {
        if (getArguments() == null
                || mColor != (Color) getArguments().get(COLOR_KEY)
                || mBrightness != (Brightness) getArguments().get(BRIGHTNESS_KEY)) {
            getArguments().putSerializable(BRIGHTNESS_KEY, mBrightness);
            getArguments().putSerializable(COLOR_KEY, mColor);
            ((PartsActivity) getActivity()).mBrightness = mBrightness;
            ((PartsActivity) getActivity()).mColor = mColor;

            getActivity().recreate();
        }
    }

}
