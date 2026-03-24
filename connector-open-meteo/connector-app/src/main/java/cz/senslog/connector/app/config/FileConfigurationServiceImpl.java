// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.app.config;

import cz.senslog.connector.app.config.api.FileConfigurationService;
import cz.senslog.connector.model.config.ConnectorDescriptor;
import cz.senslog.connector.model.config.DefaultConfig;
import cz.senslog.connector.tools.exception.UnsupportedFileException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.*;

import static java.time.format.DateTimeFormatter.ofPattern;

/**
 * The class {@code FileConfigurationServiceImpl} represents an implementation of {@link FileConfigurationService}.
 * Configuration file is in YAML format and contains two major groups: 'settings' and 'connectors'.
 * <h2>connectors</h2>
 * Each connector must contain following attributes:
 *  - name of connector
 *  - ID of fetch provider (ID is mentioned in 'settings' group)
 *  - ID of push provider (ID is mentioned in 'settings' group)
 *  - period in second when the connector will be scheduled
 *  Example:
 *      connectors:
 *          - ConnectorName:
 *              fetcher: "<id>"
 *              pusher: "<id>"
 *              period: 60
 * <h2>settings</h2>
 * Each provider must contain basic attributes:
 *  - identifier of provider
 *  - name of provider
 *  - provider class
 *  Other attributes are dynamically loaded when they are needed
 *  but must keep the key and value syntax.
 *  Example:
 *      settings:
 *          - ProviderID:
 *              name: "<name>"
 *              provider: "cz.senslog.connector.ClassProvider"
 *
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
class FileConfigurationServiceImpl extends ConfigurationServiceImpl implements FileConfigurationService {

    private static final Logger logger = LogManager.getLogger(FileConfigurationServiceImpl.class);

    /** Name of the configuration file. */
    private final String fileName;

    /**
     * Constructors sets all attributes.
     * @param fileName - name of the configuration file.
     */
    FileConfigurationServiceImpl(String fileName) {
        logger.debug("Creating a new FileConfigurationService.");
        this.fileName = fileName;
    }

    @Override
    public void load() throws IOException {
        logger.info("Loading '{}' configuration file.", fileName);

        if (!fileName.toLowerCase().endsWith(".yaml")) {
            throw new UnsupportedFileException(fileName + "does not contain .yaml extension.");
        }

        Path filePath = Paths.get(fileName);
        if (Files.notExists(filePath)) {
            throw new FileNotFoundException(fileName + " does not exist");
        }

        Map<Object, Object> properties;

        logger.debug("Opening the file '{}'.", fileName);
        try (InputStream fileStream = Files.newInputStream(filePath)) {
            logger.debug("Parsing the yaml file '{}'.", fileName);
            properties = new Yaml().load(fileStream);
            logger.debug("The configuration yaml file '{}' was parsed successfully.", fileName);
        }

        if (properties == null || properties.isEmpty()) {
            throw new IOException(String.format(
                    "The configuration yaml file %s is empty or was not loaded successfully. ", fileName
            ));
        }

        logger.debug("Getting 'settings' property from the configuration file.");
        List<?> settingsList = (List<?>)properties.get("settings");

        logger.debug("Getting 'connectors' property from the configuration file.");
        List<?> connectorsList = (List<?>) properties.get("connectors");

        logger.debug("Starting to parse all connector descriptors from the config file.");
        settings(settingsList);

        logger.debug("Starting to create all connector connection from the configuration file.");
        createConnectorDescriptors(connectorsList);

        logger.info("The configuration file '{}' was parsed successfully.", fileName);
    }

    private void settings(List<?> settingsList) throws InvalidPropertiesFormatException {

        logger.debug("Parsing 'settings' from the configuration file.");
        for (Object settings : settingsList) {
            if (!(settings instanceof Map)) {
                throw logger.throwing(new InvalidPropertiesFormatException(
                        "Property 'settings' is not in the valid format."
                ));
            }

            Map<?, ?> settingsMap = (Map<?, ?>) settings;
            for (Object settingsEntryObject : settingsMap.entrySet()) {
                if (!(settingsEntryObject instanceof Map.Entry)) {
                    throw logger.throwing(new InvalidPropertiesFormatException(
                            "Values in property 'settings' are not accessible as a dictionary."
                    ));
                }

                Map.Entry<?, ?> settingsEntry = (Map.Entry<?, ?>) settingsEntryObject;

                String descriptorId = (String) settingsEntry.getKey();

                logger.debug("Getting descriptor for the settings ID '{}'.", descriptorId);
                Object settingsValuesObject = settingsEntry.getValue();
                if (!(settingsValuesObject instanceof Map)) {
                    throw logger.throwing(new InvalidPropertiesFormatException(
                            "Values for the descriptor '"+descriptorId+"' are not accessible as a dictionary."
                    ));
                }

                Map<?, ?> settingsValuesMap = (Map<?, ?>) settingsValuesObject;

                logger.debug("Getting property 'provider' from the settings descriptor '{}'.", descriptorId);
                String providerClassStr = (String) settingsValuesMap.get("provider");
                if (providerClassStr == null) {
                    throw logger.throwing(new NoSuchElementException(
                            "Property 'provider' was not found."
                    ));
                }
                settingsValuesMap.remove("provider");

                Class<?> providerClass;
                try {
                    logger.debug("Creating a class from the provider class name {}.", providerClassStr);
                    providerClass = Class.forName(providerClassStr);
                } catch (ClassNotFoundException e) {
                    logger.catching(e);
                    continue;
                }

                logger.debug("Creating a new DefaultConfig class for the settings descriptor '{}'.", descriptorId);
                DefaultConfig defaultConfig = new DefaultConfig(descriptorId, providerClass);

                logger.debug("Starting to set all properties from the settings descriptor '{}'.", descriptorId);
                for (Object propertyEntryObject : settingsValuesMap.entrySet()) {
                    if (!(propertyEntryObject instanceof Map.Entry)) {
                        throw logger.throwing(new InvalidPropertiesFormatException(
                                        "Property values in the descriptor '"+descriptorId+"' are not accessible as a dictionary."
                        ));
                    }

                    Map.Entry<?, ?> propertyEntry = (Map.Entry<?, ?>) propertyEntryObject;

                    logger.trace("Setting property '{}' from the settings descriptor '{}'.", propertyEntry.getKey(), descriptorId);
                    defaultConfig.setProperty((String) propertyEntry.getKey(), propertyEntry.getValue());
                }

                logger.debug("Saving the settings descriptor '{}'.", descriptorId);
                addProviderConfiguration(descriptorId, defaultConfig);

            }
        }
    }

    private void createConnectorDescriptors(List<?> connectorsList) throws InvalidPropertiesFormatException {

        logger.debug("Parsing 'connectors' from the configuration file.");
        for (Object connector : connectorsList) {
            if (!(connector instanceof Map)) {
                throw logger.throwing(new InvalidPropertiesFormatException(
                        "Property 'connectors' is not in the valid format."
                ));
            }

            Map<?, ?> connectorMap = (Map<?, ?>) connector;

            for (Object connectorEntryObject : connectorMap.entrySet()) {
                if (!(connectorEntryObject instanceof Map.Entry)) {
                    throw logger.throwing(new InvalidPropertiesFormatException(
                            "Values in property 'connectors' are not accessible as a dictionary."
                    ));
                }

                Map.Entry<?,?> connectorEntry = (Map.Entry<?,?>) connectorEntryObject;

                String descriptorId = (String) connectorEntry.getKey();

                logger.debug("Getting descriptor for the connector ID '{}'.", descriptorId);
                Object connectorValuesObject = connectorEntry.getValue();
                if (!(connectorValuesObject instanceof Map)) {
                    throw logger.throwing(new InvalidPropertiesFormatException(
                            "Values for the descriptor '"+descriptorId+"' are not accessible as a dictionary."
                    ));
                }

                Map<?,?> connectorValuesMap = (Map<?,?>) connectorValuesObject;

                logger.debug("Getting the fetch class provider for the connector ID '{}'.", descriptorId);
                String fetchProviderId = (String) connectorValuesMap.get("fetcher");
                if (fetchProviderId == null) {
                    throw logger.throwing(new NoSuchElementException(
                            "Property 'fetcher' does not exist in connector descriptor '"+descriptorId+"'."
                    ));
                }

                logger.debug("Getting the push class provider for the connector ID '{}'.", descriptorId);
                String pushProviderId = (String) connectorValuesMap.get("pusher");
                if (pushProviderId == null) {
                    throw logger.throwing(new NoSuchElementException(
                            "Property 'pusher' does not exist in connector descriptor '"+descriptorId+"'."
                    ));
                }

                logger.debug("Getting property 'period' from the connector descriptor '{}'.", descriptorId);
                Integer period = (Integer) connectorValuesMap.get("period");

                logger.debug("Getting property 'initDelay' from the connector descriptor '{}'.", descriptorId);
                Integer delay = connectorValuesMap.containsKey("initDelay") ? (Integer) connectorValuesMap.get("initDelay") : null;

                logger.debug("Getting property 'startAt' from the connector descriptor '{}'.", descriptorId);
                LocalTime startAt = connectorValuesMap.containsKey("startAt") ? LocalTime.parse((String)connectorValuesMap.get("startAt"), ofPattern("HH:mm:ss")) : null;

                logger.debug("Creating a new ConnectorDescriptor class for the connector descriptor '{}'.", descriptorId);
                ConnectorDescriptor connDesc = new ConnectorDescriptor(descriptorId, fetchProviderId, pushProviderId, period, delay, startAt);

                logger.debug("Saving the connector descriptor '{}'.", descriptorId);
                addConnectorDescriptor(connDesc);
            }
        }
    }
}
