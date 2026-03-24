// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.app.config;

import cz.senslog.connector.app.config.api.FileBuilder;
import cz.senslog.connector.app.config.api.FileConfigurationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The class {@code FileBuilderImpl} represents an implementation of {@link FileBuilder}.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public class FileBuilderImpl implements FileBuilder {

    private static final Logger logger = LogManager.getLogger(FileBuilderImpl.class);

    /** Name of the file configuration. */
    private String fileName;

    /**
     * Constructor.
     */
    public FileBuilderImpl() {
        logger.debug("Creating a builder for the configuration service.");
    }

    @Override
    public FileBuilder fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    @Override
    public FileConfigurationService build() {
        logger.debug("Building a new FileConfigurationService");
        FileConfigurationService service = new FileConfigurationServiceImpl(fileName);
        logger.debug("FileConfigurationService was build successfully.");
        return service;
    }
}
