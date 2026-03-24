// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.model.openmeteo;

import com.google.gson.JsonObject;
import cz.senslog.connector.model.api.AbstractModel;

import java.time.OffsetDateTime;
import java.util.List;

public class OpenMeteoModel extends AbstractModel {

    private final List<JsonObject> data;

    public OpenMeteoModel(OffsetDateTime from, OffsetDateTime to, List<JsonObject> data) {
        super(from, to);
        this.data = data;
    }

    public List<JsonObject> getData() {
        return data;
    }
}
