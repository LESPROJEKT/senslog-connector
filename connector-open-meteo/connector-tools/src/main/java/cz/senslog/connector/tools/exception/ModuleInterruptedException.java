// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.exception;

public class ModuleInterruptedException extends RuntimeException{

    public ModuleInterruptedException(String message) {
        super(message);
    }
}
