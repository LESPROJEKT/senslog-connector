// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.app.config;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import static cz.senslog.connector.tools.util.StringUtils.isNotBlank;
import static java.lang.String.format;
import static java.nio.file.Files.notExists;
import static java.nio.file.Paths.get;

/**
 * The class {@code Parameters} represents input parameters from
 * the applications. For parsing is used {@see JCommander} library.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public final class Parameters {

    private static final Logger logger = LogManager.getLogger(Parameters.class);

    private JCommander jCommander;

    /**
     * Static method to parse input parameters.
     * @param appConfig - main configuration of the application.
     * @param args - array of parameters in format e.g. ["-cf", "fileName"].
     * @return instance of {@code Parameters}.
     * @throws IOException throws if is chosen "-cf" or "-config-file" parameter and the file does not exist.
     */
    public static Parameters parse(AppConfig appConfig, String... args) throws IOException {
        logger.debug("Parsing input parameters {}", Arrays.toString(args));

        Parameters parameters = new Parameters();
        JCommander jCommander = JCommander.newBuilder()
                .programName(appConfig.getName())
                .addObject(parameters).build();
        parameters.jCommander = jCommander;

        jCommander.parse(args);

        String configFileName = parameters.getConfigFileName();
        logger.debug("Checking existence of configuration file {}", configFileName);
        if (isNotBlank(configFileName) && notExists(get(configFileName))) {
            throw new FileNotFoundException(format("Config file %s does not exist.", configFileName));
        }

        logger.info("Parsing input parameters {} were parsed successfully.", Arrays.toString(args));
        return parameters;
    }

    @Parameter(names = {"-h", "-help"}, help = true)
    private boolean help = false;

    @Parameter(names = {"-cf", "-config-file"}, description = "Configuration file in .yaml format.")
    private String configFileName;

    /**
     * Returns name of the configuration file.
     * @return string name.
     */
    public String getConfigFileName() {
        return configFileName;
    }

    public boolean isHelp() {
        return help;
    }

    public void printHelp() {
        jCommander.usage();
    }
}
