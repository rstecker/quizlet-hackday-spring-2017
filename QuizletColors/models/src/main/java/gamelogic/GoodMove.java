package gamelogic;

import org.immutables.value.Value;

/**
 * Created by rebeccastecker on 6/12/17.
 */
@Value.Immutable
public abstract class GoodMove {
    public abstract String question();
    public abstract String answer();
    public abstract boolean youAsked();
    public abstract boolean youAnswered();

}
