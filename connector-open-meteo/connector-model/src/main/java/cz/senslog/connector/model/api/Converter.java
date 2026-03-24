// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.model.api;

/**
 * The interface {@code Converter} provides a generic functionality
 * for converter. Each class which implements which interface can be registered
 * as a converter for a connector. For converter can be used only classes which extend {@link AbstractModel}.
 *
 *
 * @param <IN> which type of class will be used as an input of a converter.
 * @param <OUT> which type of class will be used as an output of a converter.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public interface Converter<IN extends AbstractModel, OUT extends AbstractModel> {

    /**
     * Provides an interface for converting from input to output model.
     * @param model - model which is converted to output.
     * @return converted input model.
     */
    OUT convert(IN model);
}
