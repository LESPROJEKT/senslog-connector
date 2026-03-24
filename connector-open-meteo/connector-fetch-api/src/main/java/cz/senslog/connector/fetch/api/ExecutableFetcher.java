// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.fetch.api;

import cz.senslog.connector.model.api.AbstractModel;
import cz.senslog.connector.model.api.ProxySessionModel;

import java.util.Optional;

/**
 * The class contains methods to create executable fetcher (i.e., {@link ConnectorFetcher})
 *
 * @param <T> model representing the fetched data as an inherited class of the {@link AbstractModel}
 */
public class ExecutableFetcher<T extends AbstractModel> {

    private final ConnectorFetcher<? extends ProxySessionModel, T> rawFetcher;

    private final ConnectorFetcher<? extends ProxySessionModel, T> fetcher;

    public static <S extends ProxySessionModel, M extends AbstractModel> ExecutableFetcher<M> create(
            ConnectorFetcher<S, M> rawFetcher
    ) {
        return new ExecutableFetcher<>(rawFetcher, rawFetcher);
    }

    /**
     * Private constructor sets fetchers. If the session is disabled, then 'rawFetcher' and 'fetcher' are the same instances.
     * @param rawFetcher fetcher that retrieves data
     * @param fetcher fetcher that is wrap by session if enabled.
     */
    private ExecutableFetcher(
            ConnectorFetcher<? extends ProxySessionModel, T> rawFetcher,
            ConnectorFetcher<? extends ProxySessionModel, T> fetcher
    ) {
        this.rawFetcher = rawFetcher;
        this.fetcher = fetcher;
    }

    public T execute() {
        return fetcher.fetch(Optional.empty());
    }

    public ConnectorFetcher<? extends ProxySessionModel, T> getRawFetcher() {
        return rawFetcher;
    }
}
