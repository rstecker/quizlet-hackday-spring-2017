package ui;

import org.immutables.value.Value;
import org.parceler.Parcel;
import org.parceler.ParcelFactory;

import java.util.List;

/**
 * Created by rebeccastecker on 6/9/17.
 */
@Parcel(value = Parcel.Serialization.VALUE, implementations = ImmutableBoardState.class)
@Value.Immutable
public abstract class BoardState {
    public abstract String question();
    public abstract List<String> options();
    public abstract List<Player> players();

    @ParcelFactory
    public static BoardState build(String question, List<String> options, List<Player> players) {
        return ImmutableBoardState.builder()
                .build();
    }
}
