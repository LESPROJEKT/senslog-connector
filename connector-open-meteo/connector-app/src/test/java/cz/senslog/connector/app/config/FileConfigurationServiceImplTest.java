// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.app.config;

import cz.senslog.connector.app.config.api.FileConfigurationService;
import cz.senslog.connector.model.config.ConnectorDescriptor;
import cz.senslog.connector.model.config.DefaultConfig;
import cz.senslog.connector.model.config.HostConfig;
import cz.senslog.connector.tools.exception.UnsupportedFileException;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileConfigurationServiceImplTest {


    @Test
    void testParsingConfigFile() throws IOException, ClassNotFoundException, URISyntaxException {

        URI uri = ClassLoader.getSystemResource("test_valid_config.yaml").toURI();
        String configFileName = Paths.get(uri).toString();

        FileConfigurationService configService = new FileConfigurationServiceImpl(configFileName);
        configService.load();

        Set<ConnectorDescriptor> descriptors = configService.getConnectorDescriptors();

        assertEquals(1, descriptors.size());

        ConnectorDescriptor descriptor = descriptors.iterator().next();
        assertEquals("ConnectorName", descriptor.getName());
        assertEquals("FetchProviderId", descriptor.getFetcherId());
        assertEquals("PushProviderId", descriptor.getPusherId());
        assertEquals(1, descriptor.getPeriod());

        DefaultConfig fetchConfig = configService.getConfigForProviderId("FetchProviderId");
        assertEquals("FetchProviderId", fetchConfig.getId());
        assertEquals(TestFetchProviderClass.class, fetchConfig.getProvider());
        assertEquals("<name>", fetchConfig.getStringProperty("name"));
        HostConfig fetchHost = new HostConfig(fetchConfig.getPropertyConfig("host"));
        assertEquals("<fetcher_api_domain>", fetchHost.getDomain());
        assertEquals("<path>", fetchHost.getPath());

        DefaultConfig pushConfig = configService.getConfigForProviderId("PushProviderId");
        assertEquals("PushProviderId", pushConfig.getId());
        assertEquals(TestPushProviderClass.class, pushConfig.getProvider());
        assertEquals("<name>", pushConfig.getStringProperty("name"));
        HostConfig pushHost = new HostConfig(pushConfig.getPropertyConfig("host"));
        assertEquals("<pusher_api_domain>", pushHost.getDomain());
        assertEquals("<path>", pushHost.getPath());
    }

    @Test
    void load_WrongFileNameExtension_UnsupportedFileException() {

        FileConfigurationService service = new FileConfigurationServiceImpl("test.txt");

        assertThrows(UnsupportedFileException.class, service::load);
    }

    @Test
    void load_FileDoesNotExist_FileNotFoundException() {

        FileConfigurationService service = new FileConfigurationServiceImpl("test.yaml");

        assertThrows(FileNotFoundException.class, service::load);
    }
}