// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.util.schedule;

public class TaskDescription {

    private final String name;

    private final Status status;

    public TaskDescription(String name, Status status) {
        this.name = name;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "TaskDescription{" +
                "name='" + name + '\'' +
                ", status=" + status +
                '}';
    }
}
