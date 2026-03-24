// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.app;

import cz.senslog.connector.app.config.*;
import cz.senslog.connector.app.config.api.ConfigurationService;
import cz.senslog.connector.app.config.api.FileConfigurationService;
import cz.senslog.connector.fetch.ConnectorFetch;
import cz.senslog.connector.model.converter.ModelConverterProvider;
import cz.senslog.connector.push.ConnectorPush;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The class {@code Application} represents a trigger for entire application.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
class Application extends Thread {

    private static final Logger logger = LogManager.getLogger(Application.class);

    /** Attribute of basic configuration values of the application. */
    private final AppConfig appConfig;

    /** Attribute of input parameters of the application. */
    private final Parameters params;

    private CountDownLatch latch;

    private ScheduledExecutorService scheduler;

    /**
     * Initialization method to trigger the application.
     * @param args - array of parameters.
     * @return new thread of {@code Runnable}.
     * @throws IOException throws if input parameters or application configuration file can not be parsed.
     */
    static Thread init(String... args) throws IOException {
        AppConfig appConfig = AppConfig.load();
        Parameters parameters = Parameters.parse(appConfig, args);

        if (parameters.isHelp()) {
            return new Thread(parameters::printHelp);
        }

        Application app = new Application(appConfig, parameters);
        Runtime.getRuntime().addShutdownHook(new Thread(app::interrupt, "clean-app"));

        return app;
    }

    /**
     * Private constructor of the class. Accessible via static init method {@link Application#init(String...)}.
     * @param appConfig basic configuration of the application. More info of the class {@see AppConfig}.
     * @param parameters parsed input parameters of the application. More info of the class  {@see Parameters}.
     */
    private Application(AppConfig appConfig, Parameters parameters) {
        super("app");

        this.appConfig = appConfig;
        this.params = parameters;
    }

    /**
     * Override method from {@link Thread} to clean up all scheduled processes after the application is being determined.
     */
    @Override
    public void interrupt() {
        logger.info("Stopping the application {} version {}", appConfig.getName(), appConfig.getVersion());

        if (latch != null) {
            for (int i = 0; i < latch.getCount(); i++) {
                latch.countDown();
            }
        }

        logger.info("Cleaning all connector's threads.");
        if (scheduler != null) {
            scheduler.shutdownNow();
        }

        logger.info("The application was stopped.");
        super.interrupt();
    }

    /**
     * The main method executing the application.
     * 1. Contains configuration services, which provide configuration for modules.
     *      Only configuration by file is available and required passing the filename as an execution parameter.
     * 2. All configured modules as loaded and initialized by its configuration.
     * 3. The class {@link ConnectorBuilder} creates connectors that then are scheduled by the scheduler.
     *      Each connector is running within a thread separately.
     */
    @Override
    public void run() {
        logger.info("Starting the application {} version {}", appConfig.getName(), appConfig.getVersion());


        ConfigurationService configService;
        try {
            FileConfigurationService service = ConfigurationService.newFileBuilder()
                    .fileName(params.getConfigFileName()).build();

            service.load();

            configService = service;
        } catch (IOException e) {
            logger.catching(e); return;
        }


        ServiceProvider serviceProvider = new ServiceProvider(ConnectorFetch::getProvider, ConnectorPush::getProvider);
        ModelConverterProvider converterProvider = new ModelConverterProvider();
        ConnectorBuilder connectorBuilder = ConnectorBuilder.init(serviceProvider, converterProvider, configService);
        Set<Connector> connectors = connectorBuilder.createConnectors();

        if (!connectors.isEmpty()) {
            scheduler = Executors.newScheduledThreadPool(connectors.size());
            latch = new CountDownLatch(connectors.size());

            logger.info("Starting a scheduler for {} connector(s).", connectors.size());
            connectors.forEach(c -> c.schedule(scheduler, latch));

            try {
                logger.info("Waiting for the working threads.");
                latch.await();
                logger.info("All scheduled connector finished their job.");
            } catch (InterruptedException e) {
                logger.catching(e);
            }
        } else {
            logger.warn("No connectors were loaded.");
        }

        interrupt();
    }
}