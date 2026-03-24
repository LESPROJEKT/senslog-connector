// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.util.schedule;

import java.util.Set;

public interface Scheduler {

    static SchedulerBuilder createBuilder() {
        return new SchedulerBuilderImpl();
    }

    void start();
    void stop();

    Status getStatus();
    Set<TaskDescription> getTaskDescriptions();

    interface SchedulerBuilder {

        SchedulerBuilder addTask(String name, Runnable task, long period);
        SchedulerBuilder addTask(Runnable task, long period);

        Scheduler build();
    }
}
