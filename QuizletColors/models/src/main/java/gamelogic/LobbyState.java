package gamelogic;

import org.immutables.value.Value;

import java.util.List;

import ui.Player;

/**
 * Created by rebeccastecker on 6/9/17.
 */
//@Parcel(value = Parcel.Serialization.VALUE, implementations = ImmutableLobbyState.class)
@Value.Immutable
public abstract class LobbyState {
    public abstract List<Player> players();

//    @ParcelFactory
    public static LobbyState build(List<Player> players) {
        return ImmutableLobbyState.builder()
                .players(players)
                .build();
    }
}
