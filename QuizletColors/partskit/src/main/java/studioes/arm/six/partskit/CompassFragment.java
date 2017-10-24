package studioes.arm.six.partskit;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by sithel on 10/23/17.
 */

public class CompassFragment extends Fragment {

    @LayoutRes private static final int LAYOUT_ID = R.layout.fragment_compass;

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(LAYOUT_ID, container, false);
        v.findViewById(R.id.rose_1).setOnClickListener(this::onClick);
        v.findViewById(R.id.rose_2).setOnClickListener(this::onClick);
        v.findViewById(R.id.shape_1).setOnClickListener(this::onClickShape);
        v.findViewById(R.id.shape_1).setOnClickListener(this::onClickShape);
        v.findViewById(R.id.shape_2).setOnClickListener(this::onClickShape);
        v.findViewById(R.id.shape_3).setOnClickListener(this::onClickShape);
        v.findViewById(R.id.shape_4).setOnClickListener(this::onClickShape);
        v.findViewById(R.id.shape_5).setOnClickListener(this::onClickShape);
        v.findViewById(R.id.shape_6).setOnClickListener(this::onClickShape);
        v.findViewById(R.id.shape_7).setOnClickListener(this::onClickShape);
        v.findViewById(R.id.shape_8).setOnClickListener(this::onClickShape);
        return v;
    }

    public void onClickShape(View v) {
        ((CompasRose)getView().findViewById(R.id.rose_1)).setLine(v.getBackground());
    }
    public void onClick(View v) {
       if (v instanceof CompasRose) {
           ((CompasRose)v).boop();
       }
    }
}
