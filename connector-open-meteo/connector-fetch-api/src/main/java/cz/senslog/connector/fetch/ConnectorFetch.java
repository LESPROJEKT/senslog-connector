// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.fetch;

import cz.senslog.connector.fetch.api.ConnectorFetchProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * The class {@code ConnectorFetch} represents a loader for classes implement {@link ConnectorFetchProvider}.
 * For this is used technology Java Service Provider Interface (SPI).
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public final class ConnectorFetch {

    private static final Logger logger = LogManager.getLogger(ConnectorFetch.class);

    /** Map of implementations. */
    private static final Map<Class<? extends ConnectorFetchProvider>, ConnectorFetchProvider> services;

    static {
        services = loadAll();
    }

    /**
     * Loads and saves all available implementations of {@link ConnectorFetchProvider}.
     * @return Map of all implementations.
     */
    private static Map<Class<? extends ConnectorFetchProvider>, ConnectorFetchProvider> loadAll() {
        logger.debug("Getting all implementation of the class {}.", ConnectorFetchProvider.class);
        Map<Class<? extends ConnectorFetchProvider>, ConnectorFetchProvider> services = new HashMap<>();
        ServiceLoader<ConnectorFetchProvider> loader = ServiceLoader.load(ConnectorFetchProvider.class);
        for (ConnectorFetchProvider connectorProvider : loader) {
            logger.debug("Loaded the class {}.", connectorProvider.getClass());
            services.put(connectorProvider.getClass(), connectorProvider);
        }
        logger.info("Successfully loaded {} class of the {}.",  services.size(), ConnectorFetchProvider.class);
        return services;
    }

    /**
     * Returns an implementation of provider according to input class.
     * @param providerClass - class which is find an implementation.
     * @return implementation type to {@link ConnectorFetchProvider}.
     */
    public static ConnectorFetchProvider getProvider(Class<?> providerClass) {
        return services.get(providerClass);
    }
}
