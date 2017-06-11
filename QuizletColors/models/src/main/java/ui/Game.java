package ui;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;

/**
 * Created by rebeccastecker on 6/11/17.
 */

@Entity
@TypeConverters(Game.State.class)
public class Game {
    public enum State {
        WAITING,
        CAN_START,
        PLAYING;

        State() {}

        @TypeConverter public static State stringToState(String state) {
            return State.valueOf(state);
        }

        @TypeConverter public static String stateToString(State data) {
            return data.name();
        }
    }

    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "game_state")
    public State gameState;

    @ColumnInfo(name = "host_name")
    public String hostName;

    @ColumnInfo(name = "is_host")
    public boolean isHost;

    public State getState() {
        return gameState;
    }

    public boolean isHost() {
        return isHost;
    }
    public void initForHost(String hostName) {
        gameState = State.WAITING;
        this.hostName = hostName;
        this.isHost = true;
    }

    public void initForPlayer(@NonNull String name, int bondState, @NonNull String address) {
        gameState = State.WAITING;
        this.hostName = address;
        this.isHost = false;
    }
}
