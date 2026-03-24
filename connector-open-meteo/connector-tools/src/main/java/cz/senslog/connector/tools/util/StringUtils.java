// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.util;

/**
 * The class {@code StringUtils} represents set of tools for strings.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public final class StringUtils {

    /**
     * Test if input string is null or empty.
     * Examples:
     *          null -> true
     *          ""   -> true
     *          " "  -> false
     *          "a"  -> false
     *
     * @param string - input string to test.
     * @return boolean value.
     */
    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }

    /**
     * Tests if input string is null, empty or contains white characters.
     * Examples:
     *          null   -> true
     *          ""     -> true
     *          " "    -> true
     *          "    " -> true
     *          "a"    -> false
     *
     * @param string - input string to test.
     * @return boolean value.
     */
    public static boolean isBlank(String string) {
        return string == null || string.replaceAll("\\s+","").isEmpty();
    }


    /**
     *  Tests negative functionality of {@see StringUtils#isBlank}.
     * @param string - input string to test.
     * @return boolean value.
     */
    public static boolean isNotBlank(String string) {
        return !isBlank(string);
    }

}
