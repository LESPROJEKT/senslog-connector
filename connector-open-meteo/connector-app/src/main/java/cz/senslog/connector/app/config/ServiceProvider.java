// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.app.config;

import cz.senslog.connector.fetch.api.ConnectorFetchProvider;
import cz.senslog.connector.push.api.ConnectorPushProvider;

import java.util.function.Function;

/**
 * The class {@code ServiceProvider} represents a wrapper for
 * fetch and push providers.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public final class ServiceProvider {

    /** Function provides fetcher instance of input class. */
    private final Function<Class<?>, ConnectorFetchProvider> fetchProviderFnc;

    /** Function provides pusher instance of input class. */
    private final Function<Class<?>, ConnectorPushProvider> pushProviderFnc;

    /**
     * Constructor allows to set all attributes.
     * @param fetchProviderFnc - function to provider fetch instance.
     * @param pushProviderFnc - function to provide push instance.
     */
    public ServiceProvider(Function<Class<?>, ConnectorFetchProvider> fetchProviderFnc,
                           Function<Class<?>, ConnectorPushProvider> pushProviderFnc
    ) {
        this.fetchProviderFnc = fetchProviderFnc;
        this.pushProviderFnc = pushProviderFnc;
    }

    /**
     * Returns fetch instance depends on input class.
     * @param providerClass - class of fetch.
     * @return instance of fetch.
     */
    public ConnectorFetchProvider getFetchProvider(Class<?> providerClass) {
        return fetchProviderFnc.apply(providerClass);
    }

    /**
     * Returns push instance depends on input class.
     * @param providerClass - class of push.
     * @return instance of push.
     */
    public ConnectorPushProvider getPushProvider(Class<?> providerClass) {
        return pushProviderFnc.apply(providerClass);
    }
}
