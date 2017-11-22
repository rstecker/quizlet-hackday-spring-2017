package studioes.arm.six.partskit;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.os.Bundle;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import static android.view.DragEvent.ACTION_DRAG_ENDED;
import static android.view.DragEvent.ACTION_DRAG_ENTERED;
import static android.view.DragEvent.ACTION_DRAG_EXITED;
import static android.view.DragEvent.ACTION_DRAG_LOCATION;
import static android.view.DragEvent.ACTION_DRAG_STARTED;
import static android.view.DragEvent.ACTION_DROP;
import static studioes.arm.six.partskit.CompassRose.PLAYER_SHAPE_DRAWABLE_RES;

/**
 * Created by sithel on 10/29/17.
 */

public class BoardFragment extends Fragment {
    public static final String TAG = BoardFragment.class.getSimpleName();
    @LayoutRes private static final int LAYOUT_ID = R.layout.fragment_board;

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(LAYOUT_ID, container, false);
        return v;
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prepAnswer(R.id.board_a_1, view.findViewById(R.id.board_a_1));
        prepAnswer(R.id.board_a_2, view.findViewById(R.id.board_a_2));
        prepAnswer(R.id.board_a_3, view.findViewById(R.id.board_a_3));
        prepAnswer(R.id.board_a_4, view.findViewById(R.id.board_a_4));
        prepRose(getRose(1));
        prepRose(getRose(2));
        prepRose(getRose(3));
        prepRose(getRose(4));
        prepQuad(1, view.findViewById(R.id.board_backing_quad_1));
        prepQuad(2, view.findViewById(R.id.board_backing_quad_2));
        prepQuad(3, view.findViewById(R.id.board_backing_quad_3));
        prepQuad(4, view.findViewById(R.id.board_backing_quad_4));
    }

    private void prepQuad(int quadNum, View view) {
        view.setOnDragListener((v, event) -> {
            Log.d(TAG, "Backing quad " + quadNum + " just received " + event.getAction());
            CompassRose rose = getRose(quadNum);
            int action = event.getAction();
            float elevation;
            switch (action) {
                case ACTION_DRAG_STARTED:
                     elevation = getResources().getDimensionPixelSize(R.dimen.active_answer_elevation);
                    Log.i(TAG, "Rebecca : b was " + v.getElevation() + " -> " + elevation);
                    v.setElevation(elevation);
                    v.invalidate();
                case ACTION_DRAG_EXITED:
                    handleOptionBlur(v, quadNum, rose, event);
                    rose.setEnergy(0.5f);
                    break;
                case ACTION_DRAG_ENTERED:
                    handleOptionFocus(v, quadNum, rose, event);
                    break;
                case ACTION_DRAG_LOCATION:
                    break;
                case ACTION_DROP:
                    rose.boop();
                    rose.setEnergy(1f);

                    // Gets the item containing the dragged data
                    ClipData.Item item = event.getClipData().getItemAt(0);

                    int answerIndex = Integer.valueOf(event.getClipData().getDescription().getLabel().toString());
                    // Gets the text data from the item.
                    CharSequence dragData = item.getText();


                    // Displays a message containing the dragged data.
                    Toast.makeText(getContext(), "Dragged data is " + dragData + " set to quad " + quadNum, Toast.LENGTH_LONG).show();

                    handleMove(quadNum, answerIndex, dragData.toString(), rose.getPlayerColor());
                    break;
                case ACTION_DRAG_ENDED:
                    mShadow = null;
                    rose.setEnergy(0.25f);
                    elevation = getResources().getDimensionPixelSize(R.dimen.resting_answer_elevation);
                    Log.i(TAG, "Rebecca :  a was " + v.getElevation() + " -> " + elevation);
                    v.setElevation(elevation);
                    v.invalidate();
                    break;
                default:
                    Log.e(TAG, "Unknown drag action: " + action);
            }
            return true;
        });
    }


    private View.DragShadowBuilder mShadow;

    @SuppressLint("ClickableViewAccessibility")
    private void prepAnswer(@IdRes int answerId, TextView view) {
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Log.i(TAG, "START on touch listener!");
                ClipData dragData = ClipData.newPlainText(String.valueOf(answerId), view.getText());
                mShadow = new View.DragShadowBuilder(view);
                return view.startDrag(dragData, mShadow, null, 0);
            } else {
                return false;
            }
        });
    }

    private void prepRose(CompassRose rose) {
        rose.setEnergy(0.1f);
    }

    @Override public void onResume() {
        super.onResume();
        getRose(1).setPlayer(randomPlayerColor(), getRandomShape());
        getRose(1).setEnergy(0.2f);
        getRose(1).setOnClickListener((v) -> {
            getRose(1).boop();
        });
        getRose(2).setPlayer(randomPlayerColor(), getRandomShape());
        getRose(2).setEnergy(0.2f);
        getRose(2).setOnClickListener((v) -> {
            getRose(2).boop();
        });
        getRose(3).setPlayer(randomPlayerColor(), getRandomShape());
        getRose(3).setEnergy(0.2f);
        getRose(3).setOnClickListener((v) -> {
            getRose(3).boop();
        });
        getRose(4).setPlayer(randomPlayerColor(), getRandomShape());
        getRose(4).setEnergy(0.2f);
        getRose(4).setOnClickListener((v) -> {
            getRose(4).boop();
        });
    }

    private CompassRose getRose(int i) {
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

    private CompassRose.RoseColor randomPlayerColor() {
        int i = (int) Math.max(0, Math.min(CompassRose.RoseColor.values().length, Math.random() * CompassRose.RoseColor.values().length));
        return CompassRose.RoseColor.values()[i];
    }

    private @DrawableRes int getRandomShape() {
        int i = (int) Math.max(0, Math.min(PLAYER_SHAPE_DRAWABLE_RES.length, Math.random() * PLAYER_SHAPE_DRAWABLE_RES.length));
        return PLAYER_SHAPE_DRAWABLE_RES[i];
    }


    private void handleOptionFocus(View answerView, int quadNum, CompassRose rose, DragEvent event) {
        rose.setEnergy(0.75f);
        int bgColor = ContextCompat.getColor(getContext(), rose.getColorRes());
        getView().findViewById(R.id.board_back_board).setBackgroundColor(bgColor);
        getView().findViewById(R.id.board_back_board).animate().setDuration(100).alpha(0.5f).start();


    }

    private void handleOptionBlur(View answerView, int quadNum, CompassRose rose, DragEvent event) {
        getView().findViewById(R.id.board_back_board).animate().setDuration(100).alpha(0).start();
    }

    private void handleMove(int quadNumber, int answerId, String playerMove, String playerColor) {
        TextView answerView = getView().findViewById(answerId);
        getView().findViewById(R.id.board_back_board).animate().setDuration(100).alpha(0).start();

        new SpringAnimation(answerView, DynamicAnimation.TRANSLATION_X, getView().getWidth())
                .setStartVelocity(0)
                .addEndListener((animation, canceled, value, velocity) -> {
                    answerView.setText("Sharks!");
                    new SpringAnimation(answerView, DynamicAnimation.TRANSLATION_X, 0)
                            .setStartVelocity(0)
                            .setStartValue(getView().getWidth() * -1)
                            .start();
                })
                .start();
    }
}
