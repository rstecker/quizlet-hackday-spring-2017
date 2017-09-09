package ui;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by rebeccastecker on 6/11/17.
 */

@Entity
@TypeConverters(Game.State.class)
public class Game {

    public enum State {
        WAITING,
        CAN_START,
        START,
        PLAYING;

        State() {}

        @TypeConverter public static State stringToState(String state) {
            return State.valueOf(state);
        }

        @TypeConverter public static String stateToString(State data) {
            return data.name();
        }
    }

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "q_set_id")
    public long qSetId;

    @ColumnInfo(name = "game_state")
    public State gameState;

    @ColumnInfo(name = "host_name")
    public String hostName;

    @ColumnInfo(name = "set_name")
    public String qSetName;

    @ColumnInfo(name = "fact_count")
    public int factCount;

    @ColumnInfo(name = "is_host")
    public boolean isHost;

    @ColumnInfo(name = "question")
    public String question;

    @ColumnInfo(name = "answer_option_1")
    public String answerOption1;

    @ColumnInfo(name = "answer_option_2")
    public String answerOption2;

    @ColumnInfo(name = "answer_option_3")
    public String answerOption3;

    @ColumnInfo(name = "answer_option_4")
    public String answerOption4;

    @ColumnInfo(name = "selected_option")
    public String selected_option;

    @ColumnInfo(name = "selected_color")
    public String selected_color;


    public State getState() {
        return gameState;
    }
    public void setState(State state) { gameState = state;}

    public boolean isHost() {
        return isHost;
    }

    public void initForHost(String hostName, long qSetId) {
        gameState = State.WAITING;
        this.hostName = hostName;
        this.qSetId = qSetId;
        this.isHost = true;
    }

    public void initForPlayer(@NonNull String name, int bondState, @NonNull String address) {
        gameState = State.WAITING;
        this.hostName = "Game ["+name+"] / "+address+" ("+bondState+")";
        this.isHost = false;
    }

    public boolean isCurrentlySelected(@Nullable String optionText) {
        if (selected_option == null) {
            return false;
        }
        return selected_option.equals(optionText);
    }
}
