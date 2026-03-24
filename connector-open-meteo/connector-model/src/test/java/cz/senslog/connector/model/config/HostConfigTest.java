// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.model.config;

import cz.senslog.connector.tools.json.BasicJson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HostConfigTest {

    @Test
    void toString_ConvertJson_True() {

        String jsonConfig = new HostConfig("http://test.com", "test").toString();
        HostConfig config = BasicJson.jsonToObject(jsonConfig, HostConfig.class);

        assertEquals("http://test.com", config.getDomain());
        assertEquals("test", config.getPath());
    }
}