// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.util;

import java.util.function.Function;

/**
 * The interface {@code Pipe} provides functionality of pipeline {@see Pipeline}.
 * The method {@code Pipe#pipe} represents character '|'.
 *
 * @param <T> - generic type of input data.
 */
public interface Pipe<T> {

    /**
     * Converter represents '|' of idea of pipeline.
     * @param mapper - converter for pipeline's flow.
     * @param <R> - generic type of output data.
     * @return new instance of {@link Next}.
     */
    <R> Next<R> pipe(Function<? super T, ? extends R> mapper);
}