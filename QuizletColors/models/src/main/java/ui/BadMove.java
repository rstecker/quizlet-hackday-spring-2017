package ui;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by rebeccastecker on 6/12/17.
 */
@Entity
public class BadMove {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "offered_answer")
    public String offeredAnswer;

    @ColumnInfo(name = "incorrect_answer")
    public String incorrectQuestion;

    @ColumnInfo(name = "correct_question")
    public String correctQuestion;

    @ColumnInfo(name = "correct_answer")
    public String correctAnswer;

    @ColumnInfo(name = "you_were_given_bad_answer")
    public boolean youWereGivenBadAnswer;

    @ColumnInfo(name = "you_answered_poorly")
    public boolean youAnsweredPoorly;

    @ColumnInfo(name = "you_failed_to_answer")
    public boolean youFailedToAnswer;

    @ColumnInfo(name = "your_answer_went_to_someone_else")
    public boolean yourAnswerWentToSomeoneElse;
}
