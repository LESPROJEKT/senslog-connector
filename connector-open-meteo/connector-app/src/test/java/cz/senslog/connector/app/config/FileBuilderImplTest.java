// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.app.config;

import cz.senslog.connector.app.config.api.ConfigurationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileBuilderImplTest {

    @Test
    void build() {

        ConfigurationService service = ConfigurationService.newFileBuilder()
                .fileName("test.yaml").build();

        assertEquals(FileConfigurationServiceImpl.class, service.getClass());
    }
}