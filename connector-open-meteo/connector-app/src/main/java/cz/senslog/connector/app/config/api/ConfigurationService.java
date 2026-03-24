// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.app.config.api;

import cz.senslog.connector.app.config.FileBuilderImpl;
import cz.senslog.connector.model.config.ConnectorDescriptor;
import cz.senslog.connector.model.config.DefaultConfig;

import java.util.Set;

/**
 * The interface {@code ConfigurationService} provides a generic service for configuration.
 * Configuration can be gotten from a file, database or anything else.
 * Provides two crucial functionalities:
 *  - returns set of connector descriptors
 *  - returns configuration for a class
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public interface ConfigurationService {

    /**
     * Creates a builder for a configuration from a file.
     * @return new instance of {@link FileBuilder}.
     */
    static FileBuilder newFileBuilder() {
        return new FileBuilderImpl();
    }

    /**
     * @return set of connector descriptors.
     */
    Set<ConnectorDescriptor> getConnectorDescriptors();

    /**
     * Returns a configuration depends on a provider id.
     * @param providerId - identifier of provider.
     * @return default configuration for an input provider id.
     */
    DefaultConfig getConfigForProviderId(String providerId);
}
