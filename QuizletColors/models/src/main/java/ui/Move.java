package ui;

import android.arch.persistence.room.ColumnInfo;

/**
 * Created by rebeccastecker on 6/11/17.
 */

public class Move {
    @ColumnInfo(name = "option")
    public String option;

    @ColumnInfo(name = "color")
    public boolean color;
}
