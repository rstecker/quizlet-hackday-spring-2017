package com.example.myapplication.bluetooth;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.immutables.value.Value;
import org.parceler.Parcel;
import org.parceler.ParcelFactory;

import java.util.List;

/**
 * Created by rebeccastecker on 6/8/17.
 */

@Parcel(value = Parcel.Serialization.VALUE, implementations = ImmutableQCMember.class)
@Value.Immutable
public abstract class QCMember {

    public enum Reaction {
        NONE,

        WRONG_CHOICE,
        FAILED_TO_ANSWER,
        RECEIVED_BAD_ANSWER,
        WRONG_USER,
        FAILED_TO_RECEIVE_ANSWER,

        CORRECT_ANSWER_RECEIVED,
        CORRECT_ANSWER_PROVIDED
    }

    public abstract String username();

    @Nullable public abstract String color();

    @Nullable public abstract String question();

    @Nullable public abstract List<String> options();

    @Value.Auxiliary @Nullable public abstract Reaction reaction();

    @ParcelFactory
    public static QCMember build(@NonNull String username,
                                 @Nullable String color,
                                 @Nullable String question,
                                 @Nullable List<String> options,
                                 @Nullable Reaction reaction) {
        return ImmutableQCMember.builder()
                .color(color)
                .username(username)
                .question(question)
                .options(options)
                .reaction(reaction)
                .build();
    }
    public static QCMember build(@NonNull String username,
                                 @Nullable String color,
                                 @Nullable String question,
                                 @Nullable List<String> options) {
        return build(username, color, question, options, Reaction.NONE);
    }
    public static QCMember build(@NonNull String username) {
        return ImmutableQCMember.builder()
                .username(username)
                .build();
    }
}
