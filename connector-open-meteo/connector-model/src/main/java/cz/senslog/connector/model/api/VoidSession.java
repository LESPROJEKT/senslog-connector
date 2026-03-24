// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.model.api;

/**
 * The class signalizes that the session is not active and no data is expecting.
 */
public class VoidSession extends ProxySessionModel {

    public VoidSession() {
        super(false);
    }
}
