// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.util;

import java.util.function.Function;

/**
 * The class {@code PipeImpl} represents implementation of {@link Pipe}.
 *
 * @param <T> - generic type of data.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
class PipeImpl<T> implements Pipe<T> {

    /** Received data from pipeline */
    private final T data;

    PipeImpl(T data) {
        this.data = data;
    }

    @Override
    public <R> Next<R> pipe(Function<? super T, ? extends R> mapper) {
        return new NextImpl<>(mapper.apply(data));
    }
}