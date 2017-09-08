package appstate;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import org.immutables.value.Value;

/**
 * Created by rebeccastecker on 9/7/17.
 */
@Entity
public class AppState {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "qusername")
    public String qUsername;

    @ColumnInfo(name = "qtoken")
    public String qToken;

    @ColumnInfo(name = "playState")
    public String playState;
}
