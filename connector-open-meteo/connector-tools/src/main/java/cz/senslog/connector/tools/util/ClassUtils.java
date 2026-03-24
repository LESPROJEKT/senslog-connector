// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.util;

import static java.lang.String.format;

/**
 * The class {@code ClassUtils} represents set of tools for classes.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public class ClassUtils {

    /**
     * Provides type safe functionality of casting. Can be used only in case,
     * if is cast a value which is direct type of the class (inheritance is not supported).
     *
     * @param value - value to be cast.
     * @param castClass - class for casting.
     * @param <T> - generic type of casting.
     * @return casted value.
     */
    public static <T> T cast(Object value, Class<T> castClass) {
        if (value == null) { return null; }
        if (value.getClass().equals(castClass) ) {
            return (T) value;
        } else {
            throw new ClassCastException(format("Value '%s' can not be cast to %s", value, castClass));
        }
    }
}
