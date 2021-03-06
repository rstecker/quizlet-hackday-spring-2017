package com.example.myapplication.bluetooth;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.immutables.value.Value;
import org.parceler.Parcel;
import org.parceler.ParcelFactory;

import java.util.List;

/**
 * Created by rebeccastecker on 6/8/17.
 */

@Parcel(value = Parcel.Serialization.VALUE, implementations = ImmutableQCMember.class)
@Value.Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class QCMember {

    public enum Reaction {
        NONE,

        WRONG_CHOICE,
        FAILED_TO_ANSWER,
        RECEIVED_BAD_ANSWER,
        WRONG_USER,
        FAILED_TO_RECEIVE_ANSWER,

        CORRECT_ANSWER_RECEIVED,
        CORRECT_ANSWER_PROVIDED;

        int getScoreImpact() {
            if (this == CORRECT_ANSWER_PROVIDED) {
                return 1;
            } else if (this == WRONG_CHOICE || this == WRONG_USER) {
                return -1;
            }
            return 0;
        }
    }

    @JsonProperty("username") public abstract String username();

    @JsonProperty("is_host") public abstract boolean isHost();

    @JsonProperty("color") @Nullable public abstract String color();

    @JsonProperty("question") @Nullable public abstract String question();

    @JsonProperty("options") @Nullable public abstract List<String> options();

    @JsonProperty("reaction") @Value.Auxiliary @Nullable public abstract Reaction reaction();

    @JsonProperty("score") @Value.Auxiliary @Nullable public abstract Integer score();

    @JsonCreator
    @ParcelFactory
    public static QCMember build(
            @JsonProperty("username") @NonNull String username,
            @JsonProperty("is_host") boolean isHost,
            @JsonProperty("color") @Nullable String color,
            @JsonProperty("question") @Nullable String question,
            @JsonProperty("options") @Nullable List<String> options,
            @JsonProperty("reaction") @Nullable Reaction reaction,
            @JsonProperty("score") @Nullable Integer score
    ) {
        return ImmutableQCMember.builder()
                .color(color)
                .isHost(isHost)
                .username(username)
                .question(question)
                .options(options)
                .reaction(reaction)
                .score(score)
                .build();
    }

    public static QCMember build(
            @NonNull String username,
            boolean isHost,
            @Nullable String color,
            @Nullable String question,
            @Nullable List<String> options
    ) {
        return build(username, isHost, color, question, options, Reaction.NONE, 0);
    }

    public int getIntScore() {
        return score() == null ? 0 : score();
    }
    public static QCMember build(@NonNull String username, boolean isHost) {
        return ImmutableQCMember.builder()
                .username(username)
                .isHost(isHost)
                .build();
    }
}
