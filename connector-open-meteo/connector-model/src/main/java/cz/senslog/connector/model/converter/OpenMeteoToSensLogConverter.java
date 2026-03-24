// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.model.converter;

import com.google.gson.JsonObject;
import cz.senslog.connector.model.api.Converter;
import cz.senslog.connector.model.openmeteo.OpenMeteoModel;
import cz.senslog.connector.model.senslog.SensLogModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OpenMeteoToSensLogConverter implements Converter<OpenMeteoModel, SensLogModel> {

    @Override
    public SensLogModel convert(OpenMeteoModel model) {
        if (model == null) {
            return null;
        }

        if (model.getData() == null || model.getData().isEmpty()) {
            return new SensLogModel(model.getFrom(), model.getTo(), Collections.emptyList());
        }

        List<SensLogModel.PassingData> data = new ArrayList<>(model.getData().size());
        for (JsonObject prediction : model.getData()) {
            data.add(new SensLogModel.PassingData(prediction.toString()));
        }

        return new SensLogModel(model.getFrom(), model.getTo(), data);
    }
}
