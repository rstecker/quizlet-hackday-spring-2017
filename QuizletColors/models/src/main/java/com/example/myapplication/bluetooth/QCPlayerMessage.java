package com.example.myapplication.bluetooth;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.immutables.value.Value;
import org.parceler.Parcel;
import org.parceler.ParcelFactory;

/**
 * Created by rebeccastecker on 6/8/17.
 */

@Parcel(value = Parcel.Serialization.VALUE, implementations = ImmutableQCPlayerMessage.class)
@Value.Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class QCPlayerMessage {
    public enum Action {
        QUERY_GAME,

        JOIN_GAME,
        LEAVE_GAME,

        PLAYER_MOVE
    }
    @JsonProperty("msg_count") public abstract int msgCount();

    @JsonProperty("action") public abstract Action action();

    @JsonProperty("state") public abstract GameState state();

    @JsonProperty("username") @Nullable public abstract String username();

    @JsonProperty("move") @Nullable public abstract QCMove move();


    public static @NonNull QCPlayerMessage build(int msgCount, @NonNull Action action, @NonNull GameState state) {
        return ImmutableQCPlayerMessage.builder()
                .msgCount(msgCount)
                .action(action)
                .state(state)
                .build();
    }

    public static @NonNull
    QCPlayerMessage build(int msgCount, @NonNull Action action, @NonNull GameState state, @NonNull String username) {
        return ImmutableQCPlayerMessage.builder()
                .msgCount(msgCount)
                .action(action)
                .state(state)
                .username(username)
                .build();
    }

    @JsonCreator
    @ParcelFactory
    public static @NonNull QCPlayerMessage build(
            @JsonProperty("msg_count") int msgCount,
            @JsonProperty("action") @NonNull Action action,
            @JsonProperty("state") @NonNull GameState state,
            @JsonProperty("username") @Nullable String username,
            @JsonProperty("move") @Nullable QCMove move) {
        return ImmutableQCPlayerMessage.builder()
                .msgCount(msgCount)
                .action(action)
                .state(state)
                .username(username)
                .move(move)
                .build();
    }

}
