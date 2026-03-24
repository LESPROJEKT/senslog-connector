// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.model.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultConfigTest {

    @Test
    void hashCode_ProviderEqual_True() {

        DefaultConfig config1 = new DefaultConfig("1", DefaultConfig.class);
        DefaultConfig config2 = new DefaultConfig("2", DefaultConfig.class);

        assertEquals(config1, config2);
    }
}