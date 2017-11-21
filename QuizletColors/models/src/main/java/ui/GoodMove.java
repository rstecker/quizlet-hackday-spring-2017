package ui;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by rebeccastecker on 6/12/17.
 */
@Entity
public class GoodMove {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "question")
    public String question;

    @ColumnInfo(name = "answer")
    public String answer;

    @ColumnInfo(name = "asker_color")
    public String askerColor;

    @ColumnInfo(name = "answerer_color")
    public String answererColor;

    @ColumnInfo(name = "you_asked")
    public boolean youAsked;

    @ColumnInfo(name = "you_answered")
    public boolean youAnswered;

    @Override public String toString() {
        return uid + " : " + timestamp + " : " + question + " : " + answer + " : " + youAsked + " : " + youAnswered;
    }
}
