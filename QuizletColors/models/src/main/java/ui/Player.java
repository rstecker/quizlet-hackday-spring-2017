package ui;

import org.immutables.value.Value;
import org.parceler.Parcel;
import org.parceler.ParcelFactory;

/**
 * Created by rebeccastecker on 6/9/17.
 */
@Parcel(value = Parcel.Serialization.VALUE, implementations = ImmutablePlayer.class)
@Value.Immutable
public abstract class Player {
    public abstract String username();
    public abstract String color();
    public abstract boolean isHost();
    public abstract boolean isYou();

    public static Player build(String username, String color) {
        return ImmutablePlayer.builder()
                .username(username)
                .color(color)
                .build();
    }

    @ParcelFactory
    public static Player build(String username, String color, boolean isHost, boolean isYou) {
        return ImmutablePlayer.builder()
                .username(username)
                .color(color)
                .isHost(isHost)
                .isYou(isYou)
                .build();
    }
}
