// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.util;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * The interface {@code Next} provides right side of pipeline.
 * Pipeline can be ended by calling {@code Next#end} or can be chained by calling {@code Next#next}.
 *
 * @param <T> - generic type of consuming data
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public interface Next<T> {

    /**
     * Ends pipeline and consumes received data.
     * @param end - consumer of received data.
     */
    void end(Consumer<T> end);

    /**
     * Provides chain for the pipeline. Parameter is function received data and provides new data.
     * @param next - function consumes data and provides new data.
     * @param <R> - generic type of new data.
     * @return - new instance of {@link Pipe}.
     */
    <R> Pipe<R> next(Function<? super T, ? extends R> next);
}