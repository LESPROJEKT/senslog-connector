// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.fetch.openmeteo;

import cz.senslog.connector.model.config.DefaultConfig;
import cz.senslog.connector.model.config.PropertyConfig;
import cz.senslog.connector.tools.util.ClassUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenMeteoConfig {

    private static final Pattern pattern = Pattern.compile("\\$\\{(?<name>\\w+)}", Pattern.CASE_INSENSITIVE);

    public static final class AllowedStation {
        private final String url;
        private final Map<String, Param> params;
        private final int minId;
        private final int maxId;

        public AllowedStation(String url, Map<String, Param> params, int minId, int maxId) {
            this.url = url;
            this.params = params;
            this.minId = minId;
            this.maxId = maxId;
        }

        public String getUrl() {
            return url;
        }

        public Map<String, Param> getParams() {
            return params;
        }

        public int getMinId() {
            return minId;
        }

        public int getMaxId() {
            return maxId;
        }
    }

    public static final class Param {
        private final boolean isDynamic;
        private final String value;

        public Param(Object value) {
            if (value instanceof String) {
                Matcher matcher = pattern.matcher((String) value);
                if (matcher.matches()) {
                    this.isDynamic = true;
                    this.value = matcher.group("name");
                } else {
                    this.isDynamic = false;
                    this.value = (String) value;
                }
            } else if (value instanceof List) {
                Collection<?> list = (Collection<?>) value;
                List<String> res = new ArrayList<>(list.size());
                for (Object o : list) {
                    res.add(ClassUtils.cast(o, String.class));
                }
                this.isDynamic = false;
                this.value = String.join(",", res);
            } else if (value instanceof Integer) {
                this.isDynamic = false;
                this.value = ((Integer) value).toString();
            } else {
                this.isDynamic = false;
                this.value = null;
            }
        }

        public boolean isDynamic() {
            return isDynamic;
        }

        public String getValue() {
            return value;
        }
    }

    private final String url;
    private final TimeZone timeZone;
    private final Supplier<LocalDate> startDateSupplier;
    private LocalDate startDate;
    private final LocalDate endDate;
    private final int period;
    private final AllowedStation allowedStation;

    OpenMeteoConfig(DefaultConfig defaultConfig) {
        this.url = defaultConfig.getStringProperty("baseUrl");
        String startDateString = defaultConfig.getStringProperty("startDate");
        if (startDateString.equalsIgnoreCase("now")) {
            this.startDateSupplier = LocalDate::now;
            this.startDate = null;
        } else {
            this.startDateSupplier = null;
            this.startDate = LocalDate.parse(startDateString);
        }
        if (defaultConfig.containsProperty("endDate")) {
            this.endDate = LocalDate.parse(defaultConfig.getStringProperty("endDate"));
        } else {
            this.endDate = null;
        }
        this.period = defaultConfig.getIntegerProperty("period");
        this.timeZone = TimeZone.getTimeZone(defaultConfig.getStringProperty("timeZone"));

        PropertyConfig allowedStation = defaultConfig.getPropertyConfig("allowedStation");
        String url = allowedStation.getStringProperty("url");
        PropertyConfig paramsConfig = allowedStation.getPropertyConfig("params");
        Set<String> attributes = paramsConfig.getAttributes();
        Map<String, Param> params = new HashMap<>(attributes.size());
        for (String attr : attributes) {
            params.put(attr, new Param(paramsConfig.getProperty(attr)));
        }

        int minId = allowedStation.containsProperty("minId") ? allowedStation.getIntegerProperty("minId") :  Integer.MIN_VALUE;
        int maxId = allowedStation.containsProperty("maxId") ? allowedStation.getIntegerProperty("maxId") :  Integer.MAX_VALUE;

        this.allowedStation = new AllowedStation(url, params, minId, maxId);
    }

    public int getPeriod() {
        return period;
    }

    public String getUrl() {
        return url;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public LocalDate getStartDate() {
        if (startDate == null) {
            startDate = startDateSupplier.get();
        }
        return  startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public AllowedStation getAllowedStation() {
        return allowedStation;
    }
}
