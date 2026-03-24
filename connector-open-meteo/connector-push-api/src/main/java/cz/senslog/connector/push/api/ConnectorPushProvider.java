// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.push.api;

import cz.senslog.connector.model.config.DefaultConfig;
import cz.senslog.connector.push.ConnectorPush;

/**
 * The interface {@code ConnectorPushProvider} provides a generic communication interface to create a new pusher.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public interface ConnectorPushProvider {

    /**
     * Creates a new instance of {@link ConnectorPush}. This method receive default
     * configuration {@link DefaultConfig} which is used to configure the new instance of pusher.
     * @param config - default configuration.
     * @return new instance of {@link ConnectorPush}.
     */
    ConnectorPusher createPusher(DefaultConfig config);
}
