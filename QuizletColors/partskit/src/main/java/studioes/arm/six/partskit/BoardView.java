package studioes.arm.six.partskit;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import static android.view.DragEvent.ACTION_DRAG_ENDED;
import static android.view.DragEvent.ACTION_DRAG_ENTERED;
import static android.view.DragEvent.ACTION_DRAG_EXITED;
import static android.view.DragEvent.ACTION_DRAG_LOCATION;
import static android.view.DragEvent.ACTION_DRAG_STARTED;
import static android.view.DragEvent.ACTION_DROP;

/**
 * Created by rebeccastecker on 11/20/17.
 */

public class BoardView extends RelativeLayout {
    public static final String TAG = BoardView.class.getSimpleName();
    @LayoutRes
    private static final int LAYOUT_ID = R.layout.fragment_board;

    public interface IBoardListener {
        void handleMove(String playerMove, String playerColor);
    }


    private View.DragShadowBuilder mShadow;
    private IBoardListener mListener;

    public BoardView(Context context) {
        super(context);
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        inflate(context, LAYOUT_ID, this);

        View view = getRootView();
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

    public void setMoveCallback(@NonNull IBoardListener callback) {
        mListener = callback;
    }

    public void setQuestion(@NonNull String question) {
        TextView questionView = getRootView().findViewById(R.id.board_q);
        if (questionView.getText().toString().equals(question)) {
            return;
        }

        new SpringAnimation(questionView, DynamicAnimation.TRANSLATION_X, getRootView().getWidth())
                .setStartVelocity(0)
                .addEndListener((animation, canceled, value, velocity) -> {
                    questionView.setText(question);
                    new SpringAnimation(questionView, DynamicAnimation.TRANSLATION_X, 0)
                            .setStartVelocity(0)
                            .setStartValue(getRootView().getWidth() * -1)
                            .start();
                })
                .start();
    }

    public void setOptions(@NonNull List<String> strOptions) {
        if (strOptions.size() != 4) {
            Log.e(TAG, "I see a non-4 length options list somehow. BAILING! "+strOptions);
            return;
        }
        setOption(getRootView().findViewById(R.id.board_a_1), strOptions.get(0));
        setOption(getRootView().findViewById(R.id.board_a_2), strOptions.get(1));
        setOption(getRootView().findViewById(R.id.board_a_3), strOptions.get(2));
        setOption(getRootView().findViewById(R.id.board_a_4), strOptions.get(3));

    }

    public @NonNull List<String> getCurrentOptions() {
        return Arrays.asList(
                ((TextView)getRootView().findViewById(R.id.board_a_1)).getText().toString(),
                ((TextView)getRootView().findViewById(R.id.board_a_2)).getText().toString(),
                ((TextView)getRootView().findViewById(R.id.board_a_3)).getText().toString(),
                ((TextView)getRootView().findViewById(R.id.board_a_4)).getText().toString()
        );
    }
    private void setOption(@NonNull TextView view, @NonNull String option) {
        if (view.getText().toString().equals(option)) {
            return;
        }

        new SpringAnimation(view, DynamicAnimation.TRANSLATION_X, getRootView().getWidth())
                .setStartVelocity(0)
                .addEndListener((animation, canceled, value, velocity) -> {
                    view.setText(option);
                    new SpringAnimation(view, DynamicAnimation.TRANSLATION_X, 0)
                            .setStartVelocity(0)
                            .setStartValue(getRootView().getWidth() * -1)
                            .start();
                })
                .start();
    }


    public void reward(String roseColor, String powColor, String powUsername) {
        CompassRose rose = findViewWithTag(CompassRose.RoseColor.findByColorName(roseColor).colorAttr);
        if (rose == null) {
            return;
        }
        rose.setEnergy(1);
        rose.reward(CompassRose.RoseColor.findByColorName(powColor), CompassRose.getShapeBasedOnUsername(powUsername));
    }

    /**
     * @param players a list of pairs.  Max 4. First value is the color of the rose, the second is the
     *                Drawable resource int for the line
     */
    public void setPlayers(@NonNull List<studioes.arm.six.partskit.Player> players) {
        for(int i = 0; i < players.size(); ++i) {
            Log.i(TAG, "Setting player "+i+" : "+players.get(i));
            getRose(i+1).setPlayer(players.get(i));
            getRose(i+1).setTag(players.get(i).getColor().colorAttr);
        }
        for (int i = players.size(); i < 4; ++i) {
            Log.i(TAG, "Disabling player "+i);
            getRose(i+1).disable();
            getRose(i+1).setTag("");
        }
    }

    private void handleMove(int quadNumber, int answerId, String playerMove, String playerColor) {
        if (mListener != null) {
            mListener.handleMove(playerMove, playerColor);
        }
        getRootView().findViewById(R.id.board_back_board).animate().setDuration(100).alpha(0).start();

        // TODO : lock the UI till the game gives us an update!
//        TextView answerView = getRootView().findViewById(answerId);
//        new SpringAnimation(answerView, DynamicAnimation.TRANSLATION_X, getRootView().getWidth())
//                .setStartVelocity(0)
//                .start();
    }

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

    private CompassRose getRose(int i) {
        @IdRes int id = R.id.rose_1;
        if (i == 2) {
            id = R.id.rose_2;
        } else if (i == 3) {
            id = R.id.rose_3;
        } else if (i == 4) {
            id = R.id.rose_4;
        }
        return getRootView().findViewById(id);
    }

    private void prepRose(CompassRose rose) {
        rose.setEnergy(0.1f);
    }

    private void handleOptionFocus(View answerView, int quadNum, CompassRose rose, DragEvent event) {
        rose.setEnergy(0.75f);
        int bgColor = ContextCompat.getColor(getContext(), rose.getColorRes());
        getRootView().findViewById(R.id.board_back_board).setBackgroundColor(bgColor);
        getRootView().findViewById(R.id.board_back_board).animate().setDuration(100).alpha(0.5f).start();
    }

    private void handleOptionBlur(View answerView, int quadNum, CompassRose rose, DragEvent event) {
        getRootView().findViewById(R.id.board_back_board).animate().setDuration(100).alpha(0).start();
    }
}
