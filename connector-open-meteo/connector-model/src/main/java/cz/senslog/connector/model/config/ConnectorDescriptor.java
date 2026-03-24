// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.model.config;

import java.time.LocalTime;

import static cz.senslog.connector.tools.json.BasicJson.objectToJson;

/**
 * The class {@code ConnectorDescriptor} represents a configuration class for a connector.
 * According to this descriptor is created a new connector.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public class ConnectorDescriptor {

    /** Name of a connector. */
    private final String name;

    /** Class of a fetcher. */
    private final String fetcherId;

    /** class of a pusher. */
    private final String pusherId;

    /** Period for scheduling. */
    private final Integer period;

    /** Initialization delay for scheduling. */
    private final Integer delay;

    private final LocalTime startAt;

    /**
     * Constructor sets all attributes.
     * @param name - name of a connector.
     * @param fetcherId - class of a fetcher.
     * @param pusherId - class of a pusher.
     * @param period - period for scheduling.
     * @param delay - initialization delay for scheduling.
     */
    public ConnectorDescriptor(String name, String fetcherId, String pusherId, Integer period, Integer delay, LocalTime startAt) {
        this.name = name;
        this.fetcherId = fetcherId;
        this.pusherId = pusherId;
        this.period = period;
        this.delay = delay;
        this.startAt = startAt;
    }

    public String getName() {
        return name;
    }

    public String getFetcherId() {
        return fetcherId;
    }

    public String getPusherId() {
        return pusherId;
    }

    public Integer getPeriod() {
        return period;
    }

    public Integer getDelay() {
        return delay;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public String toString() {
        return objectToJson(this);
    }
}
