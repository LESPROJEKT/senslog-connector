// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.util;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The class {@code NextImpl} represents implementation of {@link Next}.
 *
 * @param <R> - generic type of data.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
class NextImpl<R> implements Next<R> {

    /** Received data from pipeline */
    private final R data;

    NextImpl(R data) {
        this.data = data;
    }

    @Override
    public void end(Consumer<R> end) {
        end.accept(data);
    }

    @Override
    public <T> Pipe<T> next(Function<? super R, ? extends T> next) {
        return new PipeImpl<>(next.apply(data));
    }
}