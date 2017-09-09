package sixarmstudios.quizletcolors.logic.player;

import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;

import java.util.Arrays;
import java.util.List;

import sixarmstudios.quizletcolors.R;

/**
 * Created by rebeccastecker on 9/8/17.
 */

public class CreatureCreation {
    //    -    private static List<String> COLORS = Arrays.asList("red", "blue", "green", "yellow",
//            -            "purple", "black", "white", "grey", "pink", "violet", "cyan", "teal");
    public enum Colors {
        RED("red", R.color.creature_red),
        BLUE("blue", R.color.creature_blue),
        GREEN("green", R.color.creature_green),
        YELLOW("yellow", R.color.creature_yellow),
        PURPLE("purple", R.color.creature_purple),
        ORANGE("orange", R.color.creature_orange),
        WHITE("white", R.color.creature_white),
        GREY("grey", R.color.creature_grey),
        PINK("pink", R.color.creature_pink),
        VIOLET("violet", R.color.creature_violet),
        CYAN("cyan", R.color.creature_cyan),
        TEAL("teal", R.color.creature_teal);
        public final @ColorRes int color;
        public final String colorName;

        Colors(String colorName, @ColorRes int color) {
            this.color = color;
            this.colorName = colorName;
        }

        public static @ColorRes int lookUp(String colorName) {
            for (Colors c : Colors.values()) {
                if (c.colorName.equals(colorName)) {
                    return c.color;
                }
            }
            return RED.color;
        }
    }

    public static final List<Colors> COLORS = Arrays.asList(Colors.values());
}
