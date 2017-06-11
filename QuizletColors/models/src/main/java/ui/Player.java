package ui;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by rebeccastecker on 6/9/17.
 */
@Entity
public class Player {
    @PrimaryKey
    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "color")
    public String color;

    @ColumnInfo(name = "is_host")
    public boolean isHost;

    @ColumnInfo(name = "is_you")
    public boolean isYou;

    public Player(String username, String color, boolean isHost, boolean isYou) {
        this.username = username;
        this.color = color;
        this.isHost = isHost;
        this.isYou = isYou;
    }

    public boolean isYou() { return isYou; }
    public boolean isHost() { return isHost; }
}
