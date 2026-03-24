// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.app.config.api;

import java.io.IOException;

/**
 * The interface {@code FileConfigurationService} provides functionality
 * which is mandatory only for a file configuration.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public interface FileConfigurationService extends ConfigurationService {

    /**
     * Loads and parses the configuration file.
     * From the configuration file is loaded configuration for each class
     * and also connector description which is used to create a new one.
     * @throws IOException throws if the configuration file is not loaded correctly.
     */
    void load() throws IOException;
}