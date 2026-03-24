// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.fetch.openmeteo;

import cz.senslog.connector.fetch.api.ConnectorFetchProvider;
import cz.senslog.connector.fetch.api.ExecutableFetcher;
import cz.senslog.connector.model.config.DefaultConfig;
import cz.senslog.connector.model.openmeteo.OpenMeteoModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static cz.senslog.connector.tools.http.HttpClient.newHttpClient;

public class ConnectorFetchOpenMeteoProvider implements ConnectorFetchProvider  {

    private static final Logger logger = LogManager.getLogger(ConnectorFetchOpenMeteoProvider.class);


    @Override
    public ExecutableFetcher<OpenMeteoModel> createExecutableFetcher(DefaultConfig defaultConfig) {
        logger.info("Initialization a new fetch provider {}.", ConnectorFetchOpenMeteoProvider.class);

        logger.debug("Creating a new configuration.");
        OpenMeteoConfig config = new OpenMeteoConfig(defaultConfig);
        logger.info("Configuration for {} was created successfully.", OpenMeteoFetcher.class);


        logger.debug("Creating a new instance of {}.", OpenMeteoFetcher.class);
        OpenMeteoFetcher fetcher = new OpenMeteoFetcher(config, newHttpClient());
        logger.info("Fetcher for {} was created successfully.", OpenMeteoFetcher.class);

        ExecutableFetcher<OpenMeteoModel> executor = ExecutableFetcher.create(fetcher);
        logger.info("Fetcher executor for {} was created successfully.", OpenMeteoFetcher.class);

        return executor;
    }
}
