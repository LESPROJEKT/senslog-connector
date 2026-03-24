// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.app.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The class {@code AppConfig} represents basic configuration of
 * the application. The configuration file is located in resources
 * and the values are connected to the properties in parent pom.xml.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public class AppConfig {

    private static final Logger logger = LogManager.getLogger(AppConfig.class);


    /** Name of the properties configuration file located in the 'resources' folder. */
    private static final String PROPERTIES_FILE_NAME = "project.properties";

    /** Attribute of loaded properties. */
    private final Properties properties;

    /**
     * Static method to load the configuration file.
     * @return new instance of {@code AppConfig}.
     * @throws IOException throws if the file is not loaded successfully.
     */
    public static AppConfig load() throws IOException {
        logger.debug("Loading application configuration file '{}'", PROPERTIES_FILE_NAME);

        Properties properties = new Properties();

        logger.debug("Getting the class loader from the class {}.", AppConfig.class.getName());
        ClassLoader loader = AppConfig.class.getClassLoader();

        logger.debug("Opening the file '{}'.", PROPERTIES_FILE_NAME);
        InputStream stream = loader.getResourceAsStream(PROPERTIES_FILE_NAME);

        logger.debug("Parsing application configuration file '{}'", PROPERTIES_FILE_NAME);
        properties.load(stream);

        logger.debug("Application configuration file was loaded successfully.");
        return new AppConfig(properties);
    }

    /**
     * Private constructor of the class. Accessible via static init method {@link AppConfig#load()}.
     * @param properties - loaded properties from the file.
     */
    private AppConfig(Properties properties) {
        this.properties = properties;
    }

    /**
     * Returns name of the application defined in pom.xml
     * @return name of the application or 'unknown'.
     */
    public String getName() {
        return getProperty("app.name", "unknown");
    }

    /**
     * Returns version of the application defined in pom.xml
     * @return version of the application or empty string.
     */
    public String getVersion() {
        return getProperty("app.version", "");
    }

    /**
     * General method the get a property .
     * @param propertyName - name of the property.
     * @param defaultValue - default value if property does not exists.
     * @return value of the property or default value.
     */
    private String getProperty(String propertyName, String defaultValue) {
        logger.debug("Getting property with the name '{}'", propertyName);
        String value = properties.getProperty(propertyName);

        if (value == null) {
            logger.debug("Property '{}' was not found. Used default property value '{}'.", propertyName, defaultValue);
            return defaultValue;
        } else {
            logger.debug("Property '{}' was loaded successfully.", propertyName);
            return value;
        }
    }
}
