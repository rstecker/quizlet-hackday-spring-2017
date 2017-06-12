package ui;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by rebeccastecker on 6/11/17.
 */

@Entity
public class Option {
    @PrimaryKey
    @ColumnInfo(name = "index")
    public int index;

    @ColumnInfo(name = "option")
    public String option;

    public Option() {
    }

    @Ignore
    public Option(int i, String s) {
        index = i;
        option = s;
    }
}
