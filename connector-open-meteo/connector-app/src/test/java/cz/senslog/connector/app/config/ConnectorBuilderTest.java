// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.app.config;

import cz.senslog.connector.app.config.api.ConfigurationService;
import cz.senslog.connector.fetch.api.ConnectorFetchProvider;
import cz.senslog.connector.fetch.api.ConnectorFetcher;
import cz.senslog.connector.fetch.api.ExecutableFetcher;
import cz.senslog.connector.model.api.AbstractModel;
import cz.senslog.connector.model.api.Converter;
import cz.senslog.connector.model.api.ConverterProvider;
import cz.senslog.connector.model.api.ProxySessionModel;
import cz.senslog.connector.model.config.ConnectorDescriptor;
import cz.senslog.connector.model.config.DefaultConfig;
import cz.senslog.connector.push.api.ConnectorPushProvider;
import cz.senslog.connector.push.api.ConnectorPusher;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.time.OffsetDateTime.MAX;
import static java.time.OffsetDateTime.MIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConnectorBuilderTest {

    private static class InputModel extends AbstractModel{ InputModel() { super(MIN, MAX); }}
    private static class OutputModel extends AbstractModel{ OutputModel() { super(MIN, MAX);}}

    private static class BasicSessionModel extends ProxySessionModel {
        public BasicSessionModel() { super(false); }
    }

    private static class TestingFetcher implements ConnectorFetcher<BasicSessionModel, OutputModel> {
        @Override public void init() { }
        @Override public OutputModel fetch(Optional<BasicSessionModel> session) { return new OutputModel(); }
    }

    private static class TestingPusher implements ConnectorPusher<InputModel> {
        @Override public void init() {}
        @Override public void push(InputModel model) {}
    }

    private final ConnectorFetchProvider defaultFetchProvider = config -> ExecutableFetcher.create(new TestingFetcher());

    private final ConnectorPushProvider defaultPushProvider = config -> new TestingPusher();

    @Test
    void createConnectors_CreateTestConnector_CreatedOneConnector() {

        ConverterProvider converterProvider = mock(ConverterProvider.class);
        when(converterProvider.getConverter(OutputModel.class, InputModel.class)).thenReturn(
                (Converter<OutputModel, InputModel>) model -> new InputModel());

        ServiceProvider serviceProvider = new ServiceProvider(aClass -> defaultFetchProvider, aClass -> defaultPushProvider);

        Set<ConnectorDescriptor> connectorDescriptors = new HashSet<>();
        connectorDescriptors.add(new ConnectorDescriptor("Test", "FetcherId", "PusherId", 1, 2, null));

        ConfigurationService configService = mock(ConfigurationService.class);
        when(configService.getConnectorDescriptors()).thenReturn(connectorDescriptors);
        when(configService.getConfigForProviderId("FetcherId")).thenReturn(mock(DefaultConfig.class));
        when(configService.getConfigForProviderId("PusherId")).thenReturn(mock(DefaultConfig.class));

        Set<Connector> connectors = ConnectorBuilder.init(serviceProvider, converterProvider, configService).createConnectors();

        assertEquals(1, connectors.size());

        Connector connector = connectors.iterator().next();
        assertEquals("Test", connector.getName());
        assertEquals(1_000, connector.getPeriod().orElse(0));
        assertEquals(2_000, connector.getInitDelay().orElse(0));
    }

    @Test
    void createConnectors_CreateTwoConnectors_CreatedTwoConnectors() {

        ConverterProvider converterProvider = mock(ConverterProvider.class);
        when(converterProvider.getConverter(OutputModel.class, InputModel.class)).thenReturn(
                (Converter<OutputModel, InputModel>) model -> new InputModel());

        ServiceProvider serviceProvider = new ServiceProvider(aClass -> defaultFetchProvider, aClass -> defaultPushProvider);

        Set<ConnectorDescriptor> connectorDescriptors = new HashSet<>();
        connectorDescriptors.add(new ConnectorDescriptor("Test1", "FetcherId", "PusherId", 1, 3, null));
        connectorDescriptors.add(new ConnectorDescriptor("Test2", "FetcherId", "PusherId", 2, 3, null));

        ConfigurationService configService = mock(ConfigurationService.class);
        when(configService.getConnectorDescriptors()).thenReturn(connectorDescriptors);
        when(configService.getConfigForProviderId("FetcherId")).thenReturn(mock(DefaultConfig.class));
        when(configService.getConfigForProviderId("PusherId")).thenReturn(mock(DefaultConfig.class));

        Set<Connector> connectors = ConnectorBuilder.init(serviceProvider, converterProvider, configService).createConnectors();

        assertEquals(2, connectors.size());
    }

    @Test
    void createConnectors_FetchProviderNull_ZeroConnectors() {

        ConverterProvider converterProvider = mock(ConverterProvider.class);
        when(converterProvider.getConverter(OutputModel.class, InputModel.class)).thenReturn(
                (Converter<OutputModel, InputModel>) model -> new InputModel());

        // fetch provider class does not exist -> null
        ServiceProvider serviceProvider = new ServiceProvider(aClass -> null, aClass -> defaultPushProvider);

        Set<ConnectorDescriptor> connectorDescriptors = new HashSet<>();
        connectorDescriptors.add(new ConnectorDescriptor("Test", "FetcherId", "PusherId", 1, 2, null));

        ConfigurationService configService = mock(ConfigurationService.class);
        when(configService.getConnectorDescriptors()).thenReturn(connectorDescriptors);
        when(configService.getConfigForProviderId("PusherId")).thenReturn(mock(DefaultConfig.class));

        Set<Connector> connectors = ConnectorBuilder.init(serviceProvider, converterProvider, configService).createConnectors();

        assertEquals(0, connectors.size());
    }

    @Test
    void createConnectors_FetchConfigNull_ZeroConnectors() {

        ConverterProvider converterProvider = mock(ConverterProvider.class);
        when(converterProvider.getConverter(OutputModel.class, InputModel.class)).thenReturn(
                (Converter<OutputModel, InputModel>) model -> new InputModel());

        ServiceProvider serviceProvider = new ServiceProvider(aClass -> defaultFetchProvider, aClass -> defaultPushProvider);

        Set<ConnectorDescriptor> connectorDescriptors = new HashSet<>();
        connectorDescriptors.add(new ConnectorDescriptor("Test", "FetcherId", "PusherId", 1, 2, null));

        ConfigurationService configService = mock(ConfigurationService.class);
        when(configService.getConnectorDescriptors()).thenReturn(connectorDescriptors);
        // fetch provider configuration does not exist -> null
        when(configService.getConfigForProviderId("FetcherId")).thenReturn(null);
        when(configService.getConfigForProviderId("PusherId")).thenReturn(mock(DefaultConfig.class));

        Set<Connector> connectors = ConnectorBuilder.init(serviceProvider, converterProvider, configService).createConnectors();

        assertEquals(0, connectors.size());
    }

    @Test
    void createConnectors_PushProviderNull_ZeroConnectors() {

        ConverterProvider converterProvider = mock(ConverterProvider.class);
        when(converterProvider.getConverter(OutputModel.class, InputModel.class)).thenReturn(
                (Converter<OutputModel, InputModel>) model -> new InputModel());

        // push provider is set to null
        ServiceProvider serviceProvider = new ServiceProvider(aClass -> defaultFetchProvider, aClass -> null);

        Set<ConnectorDescriptor> connectorDescriptors = new HashSet<>();
        connectorDescriptors.add(new ConnectorDescriptor("Test", "FetcherId", "PusherId", 1, 2, null));

        ConfigurationService configService = mock(ConfigurationService.class);
        when(configService.getConnectorDescriptors()).thenReturn(connectorDescriptors);
        when(configService.getConfigForProviderId("FetcherId")).thenReturn(mock(DefaultConfig.class));

        Set<Connector> connectors = ConnectorBuilder.init(serviceProvider, converterProvider, configService).createConnectors();

        assertEquals(0, connectors.size());
    }

    @Test
    void createConnectors_PushConfigNull_ZeroConnectors() {

        ConverterProvider converterProvider = mock(ConverterProvider.class);
        when(converterProvider.getConverter(OutputModel.class, InputModel.class)).thenReturn(
                (Converter<OutputModel, InputModel>) model -> new InputModel());

        ServiceProvider serviceProvider = new ServiceProvider(aClass -> defaultFetchProvider, aClass -> defaultPushProvider);

        Set<ConnectorDescriptor> connectorDescriptors = new HashSet<>();
        connectorDescriptors.add(new ConnectorDescriptor("Test", "FetcherId", "PusherId", 1, 2, null));

        ConfigurationService configService = mock(ConfigurationService.class);
        when(configService.getConnectorDescriptors()).thenReturn(connectorDescriptors);
        when(configService.getConfigForProviderId("FetcherId")).thenReturn(mock(DefaultConfig.class));

        // fetch provider configuration does not exist -> null
        when(configService.getConfigForProviderId("PusherId")).thenReturn(null);

        Set<Connector> connectors = ConnectorBuilder.init(serviceProvider, converterProvider, configService).createConnectors();

        assertEquals(0, connectors.size());
    }

    @Test
    void createConnectors_ConverterNull_CreatedZeroConnector() {

        ConverterProvider converterProvider = mock(ConverterProvider.class);
        // converter does not exist -> null
        when(converterProvider.getConverter(OutputModel.class, InputModel.class)).thenReturn(null);

        ServiceProvider serviceProvider = new ServiceProvider(aClass -> defaultFetchProvider, aClass -> defaultPushProvider);

        Set<ConnectorDescriptor> connectorDescriptors = new HashSet<>();
        connectorDescriptors.add(new ConnectorDescriptor("Test", "FetcherId", "PusherId", 1, 2, null));

        ConfigurationService configService = mock(ConfigurationService.class);
        when(configService.getConnectorDescriptors()).thenReturn(connectorDescriptors);
        when(configService.getConfigForProviderId("FetcherId")).thenReturn(mock(DefaultConfig.class));
        when(configService.getConfigForProviderId("PusherId")).thenReturn(mock(DefaultConfig.class));

        Set<Connector> connectors = ConnectorBuilder.init(serviceProvider, converterProvider, configService).createConnectors();

        assertEquals(0, connectors.size());
    }

    @Test
    void createConnectors_IncompatibleFetchModelClass_CreatedZeroConnector() {

        // ConnectorFetcher does not contain model class as a generic parameter
         ConnectorFetchProvider fetchProvider = config -> ExecutableFetcher.create(new ConnectorFetcher() {
            @Override public void init() {}
            @Override public AbstractModel fetch(Optional session) {return new OutputModel();}
        });

        ConverterProvider converterProvider = mock(ConverterProvider.class);
        when(converterProvider.getConverter(OutputModel.class, InputModel.class)).thenReturn(
                (Converter<OutputModel, InputModel>) model -> new InputModel());

        ServiceProvider serviceProvider = new ServiceProvider(aClass -> fetchProvider, aClass -> defaultPushProvider);

        Set<ConnectorDescriptor> connectorDescriptors = new HashSet<>();
        connectorDescriptors.add(new ConnectorDescriptor("Test", "FetcherId", "PusherId", 1, 2, null));

        ConfigurationService configService = mock(ConfigurationService.class);
        when(configService.getConnectorDescriptors()).thenReturn(connectorDescriptors);
        when(configService.getConfigForProviderId("FetcherId")).thenReturn(mock(DefaultConfig.class));
        when(configService.getConfigForProviderId("PusherId")).thenReturn(mock(DefaultConfig.class));

        Set<Connector> connectors = ConnectorBuilder.init(serviceProvider, converterProvider, configService).createConnectors();

        assertEquals(0, connectors.size());
    }

    @Test
    void createConnectors_IncompatiblePushModelClass_CreatedZeroConnector() {

        // ConnectorPusher does not contain model class as a generic parameter
        ConnectorPushProvider pushProvider = config -> new ConnectorPusher() {
            @Override public void init() {}
            @Override public void push(AbstractModel model) {}
        };

        ConverterProvider converterProvider = mock(ConverterProvider.class);
        when(converterProvider.getConverter(OutputModel.class, InputModel.class)).thenReturn(
                (Converter<OutputModel, InputModel>) model -> new InputModel());

        ServiceProvider serviceProvider = new ServiceProvider(aClass -> defaultFetchProvider, aClass -> pushProvider);

        Set<ConnectorDescriptor> connectorDescriptors = new HashSet<>();
        connectorDescriptors.add(new ConnectorDescriptor("Test", "FetcherId", "PusherId", 1, 2, null));

        ConfigurationService configService = mock(ConfigurationService.class);
        when(configService.getConnectorDescriptors()).thenReturn(connectorDescriptors);
        when(configService.getConfigForProviderId("FetcherId")).thenReturn(mock(DefaultConfig.class));
        when(configService.getConfigForProviderId("PusherId")).thenReturn(mock(DefaultConfig.class));

        Set<Connector> connectors = ConnectorBuilder.init(serviceProvider, converterProvider, configService).createConnectors();

        assertEquals(0, connectors.size());
    }
}