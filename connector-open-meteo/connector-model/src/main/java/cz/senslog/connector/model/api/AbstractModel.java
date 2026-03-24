// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.model.api;

import java.time.OffsetDateTime;

/**
 * The abstract class {@code AbstractModel} represents a base class
 * for all models which want to be used as a transfer model for a connector.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public abstract class AbstractModel {

    /** Time range from until were gotten the data. */
    private final OffsetDateTime from, to;

    protected AbstractModel(OffsetDateTime from, OffsetDateTime to) {
        this.from = from;
        this.to = to;
    }

    public OffsetDateTime getFrom() {
        return from;
    }

    public OffsetDateTime getTo() {
        return to;
    }
}
