// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.push;

import cz.senslog.connector.push.api.ConnectorPushProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * The class {@code ConnectorPush} represents a loader for classes implement {@link ConnectorPushProvider}.
 * For this is used technology Java Service Provider Interface (SPI).
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public final class ConnectorPush {

    private static final Logger logger = LogManager.getLogger(ConnectorPush.class);

    /** Map of implementations. */
    private static final Map<Class<? extends ConnectorPushProvider>, ConnectorPushProvider> services;

    static {
        services = loadAll();
    }

    /**
     * Loads and saves all available implementations of {@link ConnectorPushProvider}.
     * @return Map of all implementations.
     */
    private static Map<Class<? extends ConnectorPushProvider>, ConnectorPushProvider> loadAll() {
        logger.debug("Getting all implementation of the class {}.", ConnectorPushProvider.class);
        Map<Class<? extends ConnectorPushProvider>, ConnectorPushProvider> services = new HashMap<>();
        ServiceLoader<ConnectorPushProvider> loader = ServiceLoader.load(ConnectorPushProvider.class);
        for (ConnectorPushProvider connectorProvider : loader) {
            logger.debug("Loaded the class {}.", connectorProvider.getClass());
            services.put(connectorProvider.getClass(), connectorProvider);
        }
        logger.info("Successfully loaded {} class of the {}.",  services.size(), ConnectorPushProvider.class);
        return services;
    }

    /**
     * Returns an implementation of provider according to input class.
     * @param providerClass - class which is find an implementation.
     * @return implementation type to {@link ConnectorPushProvider}.
     */
    public static ConnectorPushProvider getProvider(Class<?> providerClass) {
        return services.get(providerClass);
    }

}
