// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.model.config;

import org.junit.jupiter.api.Test;

import static cz.senslog.connector.tools.json.BasicJson.jsonToObject;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectorDescriptorTest {

    @Test
    void toString_ConvertJson_True() {

        String jsonDescriptor = new ConnectorDescriptor("test", "FetcherId", "PusherId", 42, 2, null).toString();
        ConnectorDescriptor descriptor = jsonToObject(jsonDescriptor, ConnectorDescriptor.class);

        assertEquals("test", descriptor.getName());
        assertEquals("FetcherId", descriptor.getFetcherId());
        assertEquals("PusherId", descriptor.getPusherId());
        assertEquals(42, descriptor.getPeriod());
        assertEquals(2, descriptor.getDelay());
    }
}