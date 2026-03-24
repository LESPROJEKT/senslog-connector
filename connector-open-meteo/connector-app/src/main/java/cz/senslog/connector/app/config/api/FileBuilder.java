// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.app.config.api;

/**
 * The interface {@code FileBuilder} provides a configuration
 * to load a configuration file.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public interface FileBuilder {

    /**
     * Sets name of file to the configuration.
     * @param fileName - name of configuration file.
     * @return instance of builder {@code FileBuilder}.
     */
    FileBuilder fileName(String fileName);

    /**
     * Creates a new instance with the configuration.
     * @return new instance of {@link FileConfigurationService}.
     */
    FileConfigurationService build();
}