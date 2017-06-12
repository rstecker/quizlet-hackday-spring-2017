package gamelogic;

import org.immutables.value.Value;

/**
 * Created by rebeccastecker on 6/12/17.
 */
@Value.Immutable
public abstract class BadMove {
    public abstract String offeredAnswer();
    public abstract String incorrectQuestion();
    public abstract String correctQuestion();
    public abstract String correctAnswer();
    public abstract boolean youWereGivenBadAnswer();
    public abstract boolean youAnsweredPoorly();
    public abstract boolean youFailedToAnswer();
    public abstract boolean yourAnswerWentToSomeoneElse();
}
