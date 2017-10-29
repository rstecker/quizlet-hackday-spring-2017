package studioes.arm.six.partskit;

import android.content.ClipData;
import android.os.Bundle;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static studioes.arm.six.partskit.CompasRose.PLAYER_COLOR_ATTRS;
import static studioes.arm.six.partskit.CompasRose.PLAYER_SHAPE_DRAWABLE_RES;

/**
 * Created by sithel on 10/29/17.
 */

public class BoardFragment extends Fragment {
    public static final String TAG = BoardFragment.class.getSimpleName();
    @LayoutRes private static final int LAYOUT_ID = R.layout.fragment_board;
    private float mEnergy1 = 1;
    private float mEnergy2 = 1;
    private float mEnergy3 = 1;
    private float mEnergy4 = 1;


    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(LAYOUT_ID, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prepAnswer(view.findViewById(R.id.board_a_1));
        prepAnswer(view.findViewById(R.id.board_a_2));
        prepAnswer(view.findViewById(R.id.board_a_3));
        prepAnswer(view.findViewById(R.id.board_a_4));
        prepRose(getRose(1));
        prepRose(getRose(2));
        prepRose(getRose(3));
        prepRose(getRose(4));
    }

    private void prepAnswer(View view) {
        view.setOnClickListener((v) -> {
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            String localState = "foo";
            Log.i(TAG, "I saw a click");
//            view.startDrag(null, shadowBuilder, localState, 0);
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) {
                Log.i(TAG, "START on LongClick!");
                ClipData.Item item = new ClipData.Item("shark");
                String[] type = {"text/plain"};
                ClipData dragData = new ClipData("monkey", type, item);
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(view);
                return view.startDrag(dragData, myShadow, null, 0);
            }
        });
    }

    private void prepRose(CompasRose rose) {
        rose.setOnDragListener(rose);
    }

    @Override public void onResume() {
        super.onResume();
        getRose(1).setPlayer(randomPlayerColor(), getRandomShape());
        getRose(1).setEnergy(mEnergy1);
        getRose(1).setOnClickListener((v) -> {
            mEnergy1 = ampRose(mEnergy1);
            getRose(1).setEnergy(mEnergy1);
        });
        getRose(2).setPlayer(randomPlayerColor(), getRandomShape());
        getRose(2).setEnergy(mEnergy2);
        getRose(2).setOnClickListener((v) -> {
            mEnergy2 = ampRose(mEnergy2);
            getRose(2).setEnergy(mEnergy2);
        });
        getRose(3).setPlayer(randomPlayerColor(), getRandomShape());
        getRose(3).setEnergy(mEnergy3);
        getRose(3).setOnClickListener((v) -> {
            mEnergy3 = ampRose(mEnergy3);
            getRose(3).setEnergy(mEnergy3);
        });
        getRose(4).setPlayer(randomPlayerColor(), getRandomShape());
        getRose(4).setEnergy(mEnergy4);
        getRose(4).setOnClickListener((v) -> {
            mEnergy4 = ampRose(mEnergy4);
            getRose(4).setEnergy(mEnergy4);
        });
    }

    private float ampRose(float curLvl) {
        curLvl = curLvl + 0.1f;
        if (curLvl >= 1f) {
            return 0;
        }
        return curLvl;
    }

    private CompasRose getRose(int i) {
        @IdRes int id = R.id.rose_1;
        if (i == 2) {
            id = R.id.rose_2;
        } else if (i == 3) {
            id = R.id.rose_3;
        } else if (i == 4) {
            id = R.id.rose_4;
        }
        return getView().findViewById(id);
    }

    private @AttrRes int randomPlayerColor() {
        int i = (int) Math.max(0, Math.min(PLAYER_COLOR_ATTRS.length, Math.random() * PLAYER_COLOR_ATTRS.length));
        return PLAYER_COLOR_ATTRS[i];
    }

    private @DrawableRes int getRandomShape() {
        int i = (int) Math.max(0, Math.min(PLAYER_SHAPE_DRAWABLE_RES.length, Math.random() * PLAYER_SHAPE_DRAWABLE_RES.length));
        return PLAYER_SHAPE_DRAWABLE_RES[i];
    }
}
