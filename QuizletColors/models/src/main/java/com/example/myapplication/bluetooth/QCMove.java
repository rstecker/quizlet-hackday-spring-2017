package com.example.myapplication.bluetooth;

import android.support.annotation.NonNull;

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

@Parcel(value = Parcel.Serialization.VALUE, implementations = ImmutableQCMove.class)
@Value.Immutable
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class QCMove {
    @JsonProperty("answer") public abstract String answer();

    @JsonProperty("color") public abstract String color();

    @JsonCreator
    @ParcelFactory
    public static QCMove build(
            @JsonProperty("answer") @NonNull String answer,
            @JsonProperty("color") @NonNull String color) {
        return ImmutableQCMove.builder()
                .answer(answer)
                .color(color)
                .build();
    }
}
