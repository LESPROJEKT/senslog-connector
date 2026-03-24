// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.app.config;

import cz.senslog.connector.app.config.api.ConfigurationService;
import cz.senslog.connector.model.config.ConnectorDescriptor;
import cz.senslog.connector.model.config.DefaultConfig;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The class {@code ConfigurationServiceImpl} represents an implementation of {@link ConfigurationService}.
 * The class is used to provide a registration for some new connectors and class configurations.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public abstract class ConfigurationServiceImpl implements ConfigurationService {

    private final Set<ConnectorDescriptor> connectorDescriptors;
    private final Map<String, DefaultConfig> configurations;

    ConfigurationServiceImpl() {
        this.connectorDescriptors = new HashSet<>();
        this.configurations = new HashMap<>();
    }

    protected void addConnectorDescriptor(ConnectorDescriptor descriptor) {
        connectorDescriptors.add(descriptor);
    }

    protected void addProviderConfiguration(String providerId, DefaultConfig config) {
        if (!configurations.containsKey(providerId)) {
            configurations.put(providerId, config);
        }
    }

    @Override
    public Set<ConnectorDescriptor> getConnectorDescriptors() {
        return connectorDescriptors;
    }

    @Override
    public DefaultConfig getConfigForProviderId(String providerId) {
        return configurations.get(providerId);
    }
}
