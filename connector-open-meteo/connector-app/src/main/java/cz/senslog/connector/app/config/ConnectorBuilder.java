// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.app.config;

import cz.senslog.connector.app.config.api.ConfigurationService;
import cz.senslog.connector.model.config.ConnectorDescriptor;
import cz.senslog.connector.model.config.DefaultConfig;
import cz.senslog.connector.fetch.api.ConnectorFetchProvider;
import cz.senslog.connector.fetch.api.ConnectorFetcher;
import cz.senslog.connector.fetch.api.ExecutableFetcher;
import cz.senslog.connector.model.api.AbstractModel;
import cz.senslog.connector.model.api.Converter;
import cz.senslog.connector.model.api.ConverterProvider;
import cz.senslog.connector.push.api.ConnectorPushProvider;
import cz.senslog.connector.push.api.ConnectorPusher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;

/**
 * The class {@code ConnectorBuilder} provides a builder for the class {@link Connector}.
 * The class creates new connectors according to configuration.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public final class ConnectorBuilder {

    private static final Logger logger = LogManager.getLogger(ConnectorBuilder.class);

    /** Attribute provides fetch/push providers ({@link ServiceProvider}). */
    private final ServiceProvider serviceProvider;

    /** Attribute provides converters ({@link ConverterProvider}). */
    private final ConverterProvider converterProvider;

    /** Attribute provides configuration ({@link ConfigurationService}). */
    private final ConfigurationService configService;

    /**
     * Static method for initialization. Sets all attributes.
     * @param serviceProvider - service for fetch/push providers.
     * @param converterProvider - service for converter.
     * @param configService - service for configuration.
     * @return new instance of {@code ConnectorBuilder}.
     */
    public static ConnectorBuilder init(ServiceProvider serviceProvider, ConverterProvider converterProvider, ConfigurationService configService) {
        return new ConnectorBuilder(serviceProvider, converterProvider, configService);
    }

    /**
     * Private constructor of the class. Accessible via static init method {@link ConnectorBuilder#init(ServiceProvider, ConverterProvider, ConfigurationService)}.
     * @param serviceProvider - service for fetch/push providers.
     * @param converterProvider - service for converter.
     * @param configService - service for configuration.
     */
    private ConnectorBuilder(ServiceProvider serviceProvider, ConverterProvider converterProvider, ConfigurationService configService) {
        this.serviceProvider = serviceProvider;
        this.converterProvider = converterProvider;
        this.configService = configService;
    }

    /**
     * Creates and returns new instance of fetcher.
     * @param fetchProviderId - id of fetcher provider.
     * @return new instance of fetcher {@code ExecutableFetcher}.
     * @throws Exception throws if the fetch provider does not exist or any configuration does not exist.
     */
    private ExecutableFetcher<?> createFetcherExecutor(String fetchProviderId) throws Exception {
        logger.debug("Creating a new instance of fetcher for {}.", fetchProviderId);

        DefaultConfig config = configService.getConfigForProviderId(fetchProviderId);
        if (config == null) {
            throw logger.throwing(new Exception(format(
                    "Can not find a default settings for the provider %s.", fetchProviderId
            )));
        }

        ConnectorFetchProvider provider = serviceProvider.getFetchProvider(config.getProvider());
        if (provider == null) {
            throw logger.throwing(new Exception(format(
                    "Can not find a fetch provider instance for the %s.", config.getProvider()
            )));
        }

        return provider.createExecutableFetcher(config);
    }

    /**
     * Creates and returns new instance of pusher.
     * @param pushProviderId - class of push provider.
     * @return new instance of pusher {@code ConnectorPusher}.
     * @throws Exception throws if the push provider does not exist or any configuration does not exist.
     */
    private ConnectorPusher<?> getPusherInstance(String pushProviderId) throws Exception {
        logger.debug("Creating a new instance of pusher for {}.", pushProviderId);

        DefaultConfig config = configService.getConfigForProviderId(pushProviderId);
        if (config == null) {
            throw logger.throwing(new Exception(format(
                    "Can not find a default settings for the provider %s.", pushProviderId
            )));
        }

        ConnectorPushProvider provider = serviceProvider.getPushProvider(config.getProvider());
        if (provider == null) {
            throw logger.throwing(new Exception(format(
                    "Can not find a push provider instance for the %s.", config.getProvider()
            )));
        }

        return provider.createPusher(config);
    }

    /**
     * Creates connectors depends on configuration.
     * For each connector descriptor is loaded fetch and push provider and created fetcher and pusher.
     * From these instances is get their input model which is child of {@link AbstractModel}.
     * If everything is successful then is called #init() method and created a new connector {@link Connector}.
     * If anything throws an exception, creating of the connector will be skipped.
     * @return set of created and valid connectors.
     */
    public Set<Connector> createConnectors() {
        logger.info("Starting to create new connectors.");

        logger.debug("Getting all connector descriptors from the configuration service.");
        Set<ConnectorDescriptor> connectorDescriptors = configService.getConnectorDescriptors();
        logger.debug("Creating an empty set of connectors with init size {}.", connectorDescriptors.size());
        Set<Connector> connectors = new HashSet<>(connectorDescriptors.size());

        for (ConnectorDescriptor connDesc : connectorDescriptors) {
            try {
                logger.debug("Getting descriptors for a new '{}' connector connection.", connDesc.getName());
                logger.debug("Connector: {}", connDesc);

                ExecutableFetcher fetcherExecutor = createFetcherExecutor(connDesc.getFetcherId());
                ConnectorFetcher fetcher = fetcherExecutor.getRawFetcher();

                ConnectorPusher pusher = getPusherInstance(connDesc.getPusherId());

                Class<? extends AbstractModel> inputModel = getAbstractModelFromGeneric(fetcher.getClass());
                Class<? extends AbstractModel> outputModel = getAbstractModelFromGeneric(pusher.getClass());

                Converter converter = converterProvider.getConverter(inputModel, outputModel);
                if (converter == null) {
                    throw logger.throwing(new Exception(format(
                            "Can not find converter for connector: %s -> %s.", fetcher.getClass(), pusher.getClass()
                    )));
                }

                logger.info("Invocation of initialization method for the {}.", fetcher.getClass());
                fetcher.init();

                logger.info("Invocation of initialization method for the {}.", pusher.getClass());
                pusher.init();

                logger.debug("Creating a new {} connector.", connDesc.getName());
                Connector connector = new Connector(connDesc.getName(),
                        fetcherExecutor, pusher, converter,
                        connDesc.getPeriod(), connDesc.getDelay(), connDesc.getStartAt());

                logger.debug("Saving the {} connector.", connDesc.getName());
                connectors.add(connector);

                logger.info("New connector connection {} was created successfully.", connDesc.getName());
            } catch (Exception e) {
                logger.error("Creating of the connector {} was skipped.", connDesc.getName());
                logger.catching(e);
            }
        }
        return connectors;
    }

    /**
     * Gets a generic parameters from the input class.
     * Input class could be type of {@code ConnectorFetcher} or {@code ConnectorPusher}.
     * @param aClass - class contain generic parameters.
     * @return generic parameter extended from {@link AbstractModel}.
     * @throws Exception throws if the model can not got as a generic parameter from the input class.
     */
    @SuppressWarnings("unchecked")
    private Class<? extends AbstractModel> getAbstractModelFromGeneric(Class aClass) throws Exception {
        Type[] classTypes = aClass.getGenericInterfaces();
        if (classTypes.length == 0) {
            throw logger.throwing(new Exception(format(
                "%s does not implements any interface.", aClass
            )));
        }

        Type interfaceType = classTypes[0];
        if (!(interfaceType instanceof ParameterizedType)) {
            throw logger.throwing(new Exception(format(
                    "%s implemented interface does not contain generic parameters.", aClass
            )));
        }

        ParameterizedType parameterizedInterfaceType = (ParameterizedType) interfaceType;
        Type[] classArgumentTypes = parameterizedInterfaceType.getActualTypeArguments();
        if (classArgumentTypes.length == 0) {
            throw logger.throwing(new Exception(format(
                    "%s implements empty generic parameters.", aClass
            )));
        }

        Type classModelType; // TODO refactor
        if (classArgumentTypes.length == 2) {
            classModelType = classArgumentTypes[1];
        } else {
            classModelType = classArgumentTypes[0];
        }

        if (!(classModelType instanceof Class)) {
            throw logger.throwing(new Exception(format(
                    "%s contains generic parameters which are not instance of %s.", aClass, Class.class
            )));
        }

        Class<?> classModel = (Class<?>) classModelType;
        if (!AbstractModel.class.isAssignableFrom(classModel)) {
            throw logger.throwing(new Exception(format(
                    "%s does not contain generic parameters extended from %s", aClass, AbstractModel.class
            )));
        }

       return (Class<? extends AbstractModel>) classModel;
    }
}
