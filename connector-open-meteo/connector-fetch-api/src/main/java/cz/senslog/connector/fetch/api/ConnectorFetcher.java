// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.fetch.api;

import cz.senslog.connector.model.api.AbstractModel;
import cz.senslog.connector.model.api.ProxySessionModel;

import java.util.Optional;

/**
 * The interface {@code ConnectorFetcher} provides a generic communication interface for fetchers.
 *
 * @param <T> generic parameter of model which the method 'fetch' sens as an output.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public interface ConnectorFetcher<S extends ProxySessionModel, T extends AbstractModel> {

    /**
     * Initialization of fetcher. Method is called only once when is created a new connector.
     * @throws Exception throws when initialization is not successful.
     */
    void init() throws Exception;

    /**
     * Method is periodically scheduled and contains logic of fetcher.
     * @return model of data which was fetched.
     */
    T fetch(Optional<S> session);
}
