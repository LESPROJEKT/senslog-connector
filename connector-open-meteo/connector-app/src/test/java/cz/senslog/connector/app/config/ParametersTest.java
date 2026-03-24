// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.app.config;

import com.beust.jcommander.ParameterException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParametersTest {

    @Test
    void parse_FileParams_True() throws Exception {

        File yamlConfigFile = File.createTempFile("config_test_", ".yaml");
        String [] params = new String[] {"-cf", yamlConfigFile.getAbsolutePath()};

        AppConfig appConfig = mock(AppConfig.class);
        when(appConfig.getName()).thenReturn("Test application.");

        Parameters parameters = Parameters.parse(appConfig, params);

        assertEquals(yamlConfigFile.getAbsolutePath(), parameters.getConfigFileName());
    }

    @Test
    void parse_WrongParamName_ParameterException() throws Exception {

        File yamlConfigFile = File.createTempFile("config_test_", ".yaml");
        // 'cf' was changed to wrong parameter 'cp'
        String [] params = new String[] {"-cp", yamlConfigFile.getAbsolutePath()};

        AppConfig appConfig = mock(AppConfig.class);
        when(appConfig.getName()).thenReturn("Test application.");

        assertThrows(ParameterException.class, () -> Parameters.parse(appConfig, params));
    }

    @Test
    void parse_FileDoesNotExist_FileNotFoundException() {

        // Config file 'config.yaml' does not exist
        String [] params = new String[] {"-cf", "config.yaml"};
        AppConfig appConfig = mock(AppConfig.class);
        when(appConfig.getName()).thenReturn("Test application.");

        assertThrows(FileNotFoundException.class, () -> Parameters.parse(appConfig, params));
    }
}