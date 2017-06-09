package ui;

import org.immutables.value.Value;
import org.parceler.Parcel;
import org.parceler.ParcelFactory;

import java.util.List;

/**
 * Created by rebeccastecker on 6/9/17.
 */
@Parcel(value = Parcel.Serialization.VALUE, implementations = ImmutableLobbyState.class)
@Value.Immutable
public abstract class LobbyState {
    public abstract List<Player> players();

    @ParcelFactory
    public static LobbyState build(List<Player> players) {
        return ImmutableLobbyState.builder()
                .players(players)
                .build();
    }
}
