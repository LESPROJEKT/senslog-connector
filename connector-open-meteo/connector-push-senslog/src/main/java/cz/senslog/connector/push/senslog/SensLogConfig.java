// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.push.senslog;

import cz.senslog.connector.model.config.DefaultConfig;

public class SensLogConfig {

    private final String baseUrl;

    SensLogConfig(DefaultConfig defaultConfig) {
        this.baseUrl = defaultConfig.getStringProperty("baseUrl");
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
