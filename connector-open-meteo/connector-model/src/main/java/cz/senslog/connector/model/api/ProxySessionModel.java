// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.model.api;

public abstract class ProxySessionModel {

    private final boolean isActive;

    public ProxySessionModel(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public String toString() {
        return String.format("%s.%s", getClass().getSimpleName(), (isActive() ? "active" : "disable"));
    }
}
