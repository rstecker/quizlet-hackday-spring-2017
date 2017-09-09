package studioes.arm.six.creatures;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by sithel on 9/6/17.
 */

public class ShapeDemo  {

    public ShapeDemo() {
    }

    public View generateView(Context context, LayoutInflater layoutInflater, ViewGroup parent) {
        View v = layoutInflater.inflate(R.layout.demo_blob, parent);

        TextView tv = v.findViewById(R.id.mock_text);
        tv.setText("sharsk!");

        AppCompatImageView iv = v.findViewById(R.id.winter);
        ((Animatable)iv.getDrawable()).start();
        return v;
    }

//    public void foo() {
//        PathShape p = new PathShape();
//    }
}
