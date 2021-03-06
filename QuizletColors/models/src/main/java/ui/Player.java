package ui;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by rebeccastecker on 6/9/17.
 */
@Entity
public class Player {
    @PrimaryKey
    @ColumnInfo(name = "username")
    @NonNull public String username;

    @ColumnInfo(name = "color")
    public String color;

    @ColumnInfo(name = "is_host")
    public boolean isHost;

    @ColumnInfo(name = "is_you")
    public boolean isYou;

    @ColumnInfo(name = "score")
    public int score;

    public Player(@NonNull String username, String color, boolean isHost, boolean isYou, int score) {
        this.username = username;
        this.color = color;
        this.isHost = isHost;
        this.isYou = isYou;
        this.score = score;
    }

    public boolean isYou() { return isYou; }
    public boolean isHost() { return isHost; }
    public int score() { return score; }

    @Override public String toString() {
        return username+" : "+color+" : "+isHost()+" : "+isYou()+" : "+ score;
    }
}
