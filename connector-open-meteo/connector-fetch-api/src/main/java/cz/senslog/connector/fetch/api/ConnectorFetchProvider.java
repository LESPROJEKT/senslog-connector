// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.fetch.api;

import cz.senslog.connector.model.api.AbstractModel;
import cz.senslog.connector.model.config.DefaultConfig;

/**
 * The interface {@code ConnectorFetchProvider} provides a generic communication interface to create a new fetcher.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public interface ConnectorFetchProvider {

    /**
     * Creates a new instance of {@link ExecutableFetcher}. This method receive default
     * configuration {@link DefaultConfig} which is used to configure the new instance of a fetcher executor.
     * @param defaultConfig - default configuration.
     * @return new instance of {@link ConnectorFetcher}.
     */
    ExecutableFetcher<? extends AbstractModel> createExecutableFetcher(DefaultConfig defaultConfig);
}
