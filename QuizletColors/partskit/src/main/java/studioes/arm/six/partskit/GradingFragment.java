package studioes.arm.six.partskit;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by sithel on 10/25/17.
 */

public class GradingFragment extends Fragment {
    @LayoutRes private static final int LAYOUT_ID = R.layout.fragment_grade;
    public static final String TAG = GradingFragment.class.getSimpleName();

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(LAYOUT_ID, container, false);
        return v;
    }

    @Override public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        lockIt();
    }

    @Override public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        popIt();
    }

    @Override public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.i(TAG, "setUserVisibleHint : " + isVisibleToUser);
        if (isVisibleToUser) {
            popIt();
        } else {
            lockIt();
        }
    }

    private void lockIt() {
        Log.i(TAG, "Stop!");
        GradeBox v = findThing();
        if (v == null) {
            return;
        }
        v.lockIt();
    }
    private void popIt() {
        Log.i(TAG, "GO!");
        GradeBox v = findThing();
        if (v == null) {
            return;
        }
        v.popIt();
    }


    private @Nullable GradeBox findThing() {
        View v = getView();
        if (v == null) {
            return null;
        }
        return v.findViewById(R.id.grade_box);
    }
}
