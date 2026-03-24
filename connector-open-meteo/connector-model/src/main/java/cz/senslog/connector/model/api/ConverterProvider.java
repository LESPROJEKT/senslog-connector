// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.model.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public abstract class ConverterProvider {

    private static final Logger logger = LogManager.getLogger(ConverterProvider.class);

    private static class Wrapper {
        private final Class<?> input;
        private final Class<?> output;
        private final Converter<?,?> converter;
        private Wrapper(Class<?> input, Class<?> output, Converter<?,?> converter) {
            this.input = input;
            this.output = output;
            this.converter = converter;
        }
    }

    private final List<Wrapper> CONVERTERS = new ArrayList<>();

    protected ConverterProvider() {
        config();
    }

    protected abstract void config();

    protected void register(Class<? extends Converter<?,?>> converterClass) {
        logger.debug("Registering a new converter {}", converterClass);
        try {
            logger.debug("Getting a generic parameters from the class {}.", converterClass);
            ParameterizedType converterTypes = (ParameterizedType) converterClass.getGenericInterfaces()[0];
            Class<?> fetchModel = (Class<?>) converterTypes.getActualTypeArguments()[0];
            Class<?> pushModel = (Class<?>) converterTypes.getActualTypeArguments()[1];

            logger.debug("Creating a new instance of the class {}.", converterClass);
            Converter<?,?> converter = converterClass.newInstance();

            CONVERTERS.add(new Wrapper(fetchModel, pushModel, converter));
            logger.info("Registered a new converter {} for {} -> {}.", converterClass.getSimpleName(), fetchModel.getSimpleName(), pushModel.getSimpleName());
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("Can not create an instance for {}.", converterClass);
            logger.catching(e);
        }

    }

    public Converter getConverter(Class<? extends AbstractModel> fetchModel, Class<? extends AbstractModel> pushModel) {
        logger.info("Getting a converter for {} -> {}. ", fetchModel.getSimpleName(), pushModel.getSimpleName());
        for (Wrapper item : CONVERTERS) {
            if (item.input.equals(fetchModel) && item.output.equals(pushModel)) {
                return item.converter;
            }
        }
        logger.warn("The converter for {} -> {} was not found.", fetchModel, pushModel);
        return null;
    }
}
