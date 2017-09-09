package gamelogic;

import android.support.annotation.Nullable;

import org.immutables.value.Value;

import java.util.List;

import ui.Player;

/**
 * Created by rebeccastecker on 6/9/17.
 */
//@Parcel(value = Parcel.Serialization.VALUE, implementations = ImmutableLobbyState.class)
@Value.Immutable
public abstract class LobbyState {

    @Nullable public abstract String setName();
    @Nullable public abstract Integer factCount();
    public abstract List<Player> players();

//    @ParcelFactory
    public static LobbyState build(@Nullable String setName,
                                   @Nullable Integer factCount,
                                   List<Player> players) {
        return ImmutableLobbyState.builder()
                .setName(setName)
                .factCount(factCount)
                .players(players)
                .build();
    }
}
