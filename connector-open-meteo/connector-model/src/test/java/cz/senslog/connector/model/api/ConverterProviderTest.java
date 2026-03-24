// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.model.api;

import org.junit.jupiter.api.Test;

import static java.time.OffsetDateTime.MAX;
import static java.time.OffsetDateTime.MIN;
import static org.junit.jupiter.api.Assertions.*;

class ConverterProviderTest {

    static class InputModel extends AbstractModel {
        InputModel() {
            super(MIN, MAX);
        }
    }
    static class OutputModel extends AbstractModel {
        OutputModel() {
            super(MIN, MAX);
        }
    }

    static class TestConverter implements Converter<InputModel, OutputModel> {
        @Override public OutputModel convert(InputModel model) {return new OutputModel();}
    }

    @Test
    void registration_AddedConverter_True() {

        ConverterProvider provider = new ConverterProvider() {
            @Override
            protected void config() {
                register(TestConverter.class);
            }
        };

        Converter converter = provider.getConverter(InputModel.class, OutputModel.class);

        assertNotNull(converter);
        assertEquals(TestConverter.class, converter.getClass());
    }

    @Test
    void registration_NotRegisteredConverter_Null() {

        ConverterProvider provider = new ConverterProvider() {
            @Override protected void config() {}
        };

        Converter converter = provider.getConverter(InputModel.class, OutputModel.class);

        assertNull(converter);
    }

    @Test
    void registration_NotFoundConverter_Null() {

        ConverterProvider provider = new ConverterProvider() {
            @Override
            protected void config() {
                register(TestConverter.class);
            }
        };

        Converter converter = provider.getConverter(OutputModel.class, InputModel.class);

        assertNull(converter);
    }
}