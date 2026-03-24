// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.push.api;

import cz.senslog.connector.model.api.AbstractModel;

/**
 * The interface {@code ConnectorPusher} provides a generic communication interface for pushers.
 *
 * @param <T> generic parameter of model which the method 'push' receives as an input.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public interface ConnectorPusher<T extends AbstractModel> {

    /**
     * Initialization of pusher. Method is called only once when is created a new connector.
     * @throws Exception throws when initialization is not successful.
     */
    void init() throws Exception;

    /**
     * Method is periodically scheduled and contains logic of pusher.
     * @param model - input model contains information to be send.
     */
    void push(T model);
}
