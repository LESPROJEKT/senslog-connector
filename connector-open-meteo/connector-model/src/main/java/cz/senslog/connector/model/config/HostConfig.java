// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.model.config;

import static cz.senslog.connector.tools.json.BasicJson.objectToJson;

/**
 * The class {@code HostConfig} represents a configuration class.
 * Contains basic information which are needed to create an url.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public class HostConfig {

    /** Domain of the host. */
    private final String domain;

    /** Path of the host. */
    private final String path;

    /**
     * Constructor sets all attributes.
     * @param domain - domain of the host.
     * @param path - path of the host.
     */
    public HostConfig(String domain, String path) {
        this.domain = domain;
        this.path = path;
    }

    /**
     * Constructor sets all attributes from generic property class {@link PropertyConfig}.
     * @param config - generic configuration.
     */
    public HostConfig(PropertyConfig config) {
        this.domain = config.getStringProperty("domain");
        this.path = config.getStringProperty("path");
    }

    public String getDomain() {
        return domain;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return objectToJson(this);
    }
}
