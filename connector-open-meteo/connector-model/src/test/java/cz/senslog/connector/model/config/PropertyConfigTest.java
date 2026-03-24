// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.model.config;

import cz.senslog.connector.tools.exception.PropertyNotFoundException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class PropertyConfigTest {

    @Test
    void property_basicDataTypes_True() {

        PropertyConfig config = new PropertyConfig("test");
        config.setProperty("string", "testString");
        config.setProperty("integer", 42);

        LocalDateTime localDateTime = LocalDateTime.of(1970, Month.JANUARY, 1, 0,0, 0);
        config.setProperty("localDateTime", localDateTime);

        String date = localDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
        config.setProperty("date", date);

        assertEquals("test", config.getId());
        assertEquals("testString", config.getStringProperty("string"));
        assertEquals(42, config.getIntegerProperty("integer"));
        assertEquals(localDateTime, config.getLocalDateTimeProperty("localDateTime"));
        assertEquals(localDateTime, config.getLocalDateTimeProperty("date"));
    }

    @Test
    void property_configProperty_True() {

        PropertyConfig config = new PropertyConfig("test");
        config.setProperty("values", new HashMap<String, Integer>(){{put("integer", 42);}});

        PropertyConfig valuesConfig = config.getPropertyConfig("values");

        assertEquals("test.values", valuesConfig.getId());
        assertEquals(42, valuesConfig.getIntegerProperty("integer"));
    }

    @Test
    void property_NotFound_PropertyNotFoundException() {

        PropertyConfig config = new PropertyConfig("test");

        assertThrows(PropertyNotFoundException.class, () -> config.getProperty("unknown"));
    }

    @Test
    void property_InvalidDataType_ClassCastException() {

        PropertyConfig config = new PropertyConfig("test");
        config.setProperty("date", 42);

        assertThrows(ClassCastException.class, () -> config.getLocalDateTimeProperty("date"));
    }

    @Test
    void property_OptionalLocalDateTime_EmptyOptional() {

        PropertyConfig config = new PropertyConfig("test");

        assertFalse(config.getOptionalLocalDateTimeProperty("date").isPresent());
    }

    @Test
    void property_LocalDateTime_True() {

        PropertyConfig config = new PropertyConfig("test");
        config.setProperty("date", LocalDateTime.MIN);

        assertEquals(LocalDateTime.MIN, config.getOptionalLocalDateTimeProperty("date").orElse(null));
    }
}