// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.model.senslog;

import cz.senslog.connector.model.api.AbstractModel;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SensLogModel extends AbstractModel {

    public static final class PassingData {
        private final Map<String, String> params;
        private final String payload;

        public PassingData(Map<String, String> params, String payload) {
            Objects.requireNonNull(params);
            Objects.requireNonNull(payload);
            this.params = params;
            this.payload = payload;
        }

        public PassingData(String payload) {
            this(Collections.emptyMap(), payload);
        }

        public Map<String, String> getParams() {
            return params;
        }

        public String getPayload() {
            return payload;
        }
    }

    private final List<PassingData> passThroughData;

    public SensLogModel(OffsetDateTime from, OffsetDateTime to, List<PassingData> passThroughData) {
        super(from, to);
        this.passThroughData = passThroughData;
    }

    public List<PassingData> getPassThroughData() {
        return passThroughData;
    }
}
