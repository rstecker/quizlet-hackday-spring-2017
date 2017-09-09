package ui;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * We intentionally don't track the term ID because we don't want duplicate questions, even if there
 * are dup questions in the set :P
 *
 * Created by rebeccastecker on 6/11/17.
 */

@Entity(indices = {@Index(value = {"q_set_id", "question", "answer"},
        unique = true)})
public class Fact {
    @PrimaryKey(autoGenerate =  true)
    public int uid;

    @ColumnInfo(name = "q_set_id")
    public long qSetId;

    @ColumnInfo(name = "question")
    public String question;

    @ColumnInfo(name = "answer")
    public String answer;

    @ColumnInfo(name = "q_set_name")
    public String qSetName;

    public Fact() {
    }

    @Ignore
    public Fact(long setId, @NonNull String setName, @NonNull String question, @NonNull String answer) {
        this.qSetId = setId;
        this.qSetName = setName;
        this.question = question;
        this.answer = answer;
    }
}
