// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.model.config;

/**
 * The class {@code DefaultConfig} represents a major configuration class for all providers.
 * Represents a root node of configuration for a class defines as a {@see #provider}.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public class DefaultConfig extends PropertyConfig {

    /** Provider for which is gotten a configuration. */
    private final Class<?> provider;

    /**
     * Constructors sets root name (id) and provider class.
     * @param id - identifier of root node.
     * @param provider - class provider.
     */
    public DefaultConfig(String id, Class<?> provider) {
        super(id);
        this.provider = provider;
    }

    public Class<?> getProvider() {
        return provider;
    }

    @Override
    public int hashCode() {
        return provider.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultConfig that = (DefaultConfig) o;
        return hashCode() == that.hashCode();
    }
}
