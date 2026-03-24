// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.util;

import java.util.function.Supplier;

/**
 * The interface {@code Pipeline} provides generic functionality of pipeline.
 * This interfaces creates start of a pipeline and provides {@link Pipe}.
 * Idea of pipeline:
 *  of | end
 *  of | next | end
 *  of | next | next | end
 * Basic example:
 *  Pipeline.of(<provide_data>).pipe(<convert_data>).end(<consume_data>);
 *  Advanced example:
 *  Pipeline.of(<provide_data>).pipe(<convert_data>).next(<consume_and_provide_new_data>).pipe(<convert_data>).end(<consume_data>);
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public interface Pipeline {

    /**
     * Start method of pipeline.
     * @param start - supplier provides data.
     * @param <T> - generic type of data.
     * @return new instance of {@link Pipe}.
     */
    static <T> Pipe<T> of(Supplier<T> start) {
        return new PipeImpl<>(start.get());
    }
}
