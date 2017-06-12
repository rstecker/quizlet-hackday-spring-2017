package gamelogic;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.immutables.value.Value;

import java.util.List;

import ui.Player;

/**
 * Created by rebeccastecker on 6/9/17.
 */
@Value.Immutable
public abstract class BoardState {

    public abstract String question();

    public abstract List<String> options();

    public abstract List<Player> players();

    @Nullable public abstract BadMove badMove();
    @Nullable public abstract GoodMove goodMove();

    public static BoardState build(
                                   @NonNull String question,
                                   @NonNull List<String> options,
                                   @NonNull List<Player> players,
                                   @Nullable GoodMove goodMove,
                                   @Nullable BadMove badMove) {
        return ImmutableBoardState.builder()
                .question(question)
                .options(options)
                .players(players)
                .build();
    }
}
