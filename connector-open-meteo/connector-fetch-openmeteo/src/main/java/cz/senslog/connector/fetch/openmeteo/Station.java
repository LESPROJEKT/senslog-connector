// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.fetch.openmeteo;

public class Station {

    private final double latitude, longitude;

    public Station(double longitudeX, double latitudeY) {
        this.latitude = latitudeY;
        this.longitude = longitudeX;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}