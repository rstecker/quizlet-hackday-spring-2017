package com.example.myapplication.bluetooth;

import org.immutables.value.Value;
import org.parceler.Parcel;
import org.parceler.ParcelFactory;

/**
 * Created by rebeccastecker on 6/8/17.
 */

@Parcel(value = Parcel.Serialization.VALUE, implementations = ImmutableQCMove.class)
@Value.Immutable
public abstract class QCMove {
    public abstract String answer();
    public abstract String color();

    @ParcelFactory
    public static QCMove build(String answer, String color) {
        return ImmutableQCMove.builder()
                .answer(answer)
                .color(color)
                .build();
    }
}
