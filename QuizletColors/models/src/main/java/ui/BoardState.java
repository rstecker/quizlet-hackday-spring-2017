package ui;

import android.support.annotation.NonNull;

import org.immutables.value.Value;

import java.util.List;

/**
 * Created by rebeccastecker on 6/9/17.
 */
//@Parcel(value = Parcel.Serialization.VALUE, implementations = ImmutableBoardState.class)
@Value.Immutable
public abstract class BoardState {
    public abstract String question();

    public abstract List<String> options();

    public abstract List<Player> players();

    //    @ParcelFactory
    public static BoardState build(@NonNull String question,
                                   @NonNull List<String> options,
                                   @NonNull List<Player> players) {
        return ImmutableBoardState.builder()
                .question(question)
                .options(options)
                .players(players)
                .build();
    }
}
