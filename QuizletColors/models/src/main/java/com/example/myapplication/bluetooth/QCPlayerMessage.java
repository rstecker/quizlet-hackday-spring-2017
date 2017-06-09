package com.example.myapplication.bluetooth;

import android.support.annotation.Nullable;

import org.immutables.value.Value;
import org.parceler.Parcel;
import org.parceler.ParcelFactory;

/**
 * Created by rebeccastecker on 6/8/17.
 */

@Parcel(value = Parcel.Serialization.VALUE, implementations = ImmutableQCPlayerMessage.class)
@Value.Immutable
public abstract class QCPlayerMessage {
    public enum Action {
        QUERY_GAME,

        JOIN_GAME,
        LEAVE_GAME,

        PLAYER_MOVE
    }

    public abstract Action action();
    public abstract GameState state();

    @Nullable public abstract String username();
    @Nullable public abstract QCMove move();


    @ParcelFactory
    public static QCPlayerMessage build() {
        return ImmutableQCPlayerMessage.builder()
                .build();
    }

}
