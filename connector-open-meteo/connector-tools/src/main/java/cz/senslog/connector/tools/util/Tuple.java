// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.util;

import java.util.Objects;

/**
 * The class {@code Tuple} represents an accumulator for two values.
 * Each value can be in different type.
 *
 * @param <A> type of the first value.
 * @param <B> type of the second value.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public class Tuple<A, B> {

    /** First value. */
    private final A item1;

    /** Second value */
    private final B item2;

    /**
     * Factory method to create a new instance.
     * @param item1 first value.
     * @param item2 second value.
     * @param <A> type of the first value.
     * @param <B> type of the second value.
     * @return new instance of {@code Tuple}.
     */
    public static <A, B> Tuple<A, B> of(A item1, B item2) {
        return new Tuple<>(item1, item2);
    }

    /**
     * Constructor of the class sets all attributes.
     * @param item1 first value.
     * @param item2 second value.
     */
    private Tuple(A item1, B item2) {
        this.item1 = item1;
        this.item2 = item2;
    }

    public A getItem1() {
        return item1;
    }

    public B getItem2() {
        return item2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return item1.equals(tuple.item1) &&
                item2.equals(tuple.item2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item1, item2);
    }
}
