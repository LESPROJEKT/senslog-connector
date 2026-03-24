// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.model.converter;

import cz.senslog.connector.model.api.ConverterProvider;

/**
 * The class {@see ModelConverterProvider} represents a configuration class
 * for all converters which can be used for models between fetchers and pushers.
 * The method {@see ModelConverterProvider#config} provides the registration
 * for a new converter.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public class ModelConverterProvider extends ConverterProvider {

    @Override
    protected void config() {
        register(OpenMeteoToSensLogConverter.class);
    }
}
