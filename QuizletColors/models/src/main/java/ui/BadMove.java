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

    @ColumnInfo(name = "offered_answer_color")
    public String offeredAnswerColor;

    @ColumnInfo(name = "incorrect_answer")
    public String incorrectQuestion;

    @ColumnInfo(name = "incorrect_answer_color")
    public String incorrectQuestionColor;

    @ColumnInfo(name = "correct_question")
    public String correctQuestion;

    @ColumnInfo(name = "correct_question_color")
    public String correctQuestionColor;

    @ColumnInfo(name = "correct_answer")
    public String correctAnswer;

    @ColumnInfo(name = "correct_answer_color")
    public String correctAnswerColor;

    @ColumnInfo(name = "you_were_given_bad_answer")
    public boolean youWereGivenBadAnswer;

    @ColumnInfo(name = "you_answered_poorly")
    public boolean youAnsweredPoorly;

    @ColumnInfo(name = "you_failed_to_answer")
    public boolean youFailedToAnswer;

    @ColumnInfo(name = "your_answer_went_to_someone_else")
    public boolean yourAnswerWentToSomeoneElse;

    @Override
    public String toString() {
        return new StringBuilder("\n")
                .append("Offered Answer: ").append(offeredAnswer).append(" [").append(offeredAnswerColor).append("]").append("\n")
                .append("Incorrect Question: ").append(incorrectQuestion).append(" [").append(incorrectQuestionColor).append("]").append("\n")
                .append("Correct Question: ").append(correctQuestion).append(" [").append(correctQuestionColor).append("]").append("\n")
                .append("Correct Answer: ").append(correctAnswer).append(" [").append(correctAnswerColor).append("]").append("\n")
                .append("Given Bad Answer: ").append(youWereGivenBadAnswer).append("\n")
                .append("Answered Poorly: ").append(youAnsweredPoorly).append("\n")
                .append("Failed to Answer: ").append(youFailedToAnswer).append("\n")
                .append("Answer went to someone else: ").append(yourAnswerWentToSomeoneElse)
                .toString();
    }
}
