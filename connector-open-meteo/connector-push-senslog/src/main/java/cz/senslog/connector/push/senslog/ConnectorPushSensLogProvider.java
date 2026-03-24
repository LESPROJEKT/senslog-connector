// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.push.senslog;

import cz.senslog.connector.model.config.DefaultConfig;
import cz.senslog.connector.push.api.ConnectorPushProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static cz.senslog.connector.tools.http.HttpClient.newHttpClient;

public class ConnectorPushSensLogProvider implements ConnectorPushProvider {

    private static final Logger logger = LogManager.getLogger(ConnectorPushSensLogProvider.class);


    @Override
    public SensLogPusher createPusher(DefaultConfig config) {
        logger.info("Initialization a new push provider {}.", ConnectorPushSensLogProvider.class);

        logger.debug("Creating a new configuration.");
        SensLogConfig defaultConfig = new SensLogConfig(config);
        logger.info("Configuration for {} was created successfully.", SensLogPusher.class);

        logger.debug("Creating a new instance of {}.", SensLogPusher.class);
        SensLogPusher pusher = new SensLogPusher(defaultConfig, newHttpClient());
        logger.info("Pusher for {} was created successfully.", SensLogPusher.class);

        return pusher;
    }
}
