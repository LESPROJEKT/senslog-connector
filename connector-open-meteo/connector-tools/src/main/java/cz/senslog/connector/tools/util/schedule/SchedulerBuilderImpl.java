// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.util.schedule;

import java.util.HashSet;
import java.util.Set;

public class SchedulerBuilderImpl implements Scheduler.SchedulerBuilder {

    private final Set<ScheduleTask> tasks;

    public SchedulerBuilderImpl() {
        this.tasks = new HashSet<>();
    }

    @Override
    public Scheduler.SchedulerBuilder addTask(String name, Runnable task, long period) {
        tasks.add(new ScheduleTask(name, task, period));
        return this;
    }

    @Override
    public Scheduler.SchedulerBuilder addTask(Runnable task, long period) {
        tasks.add(new ScheduleTask(task.getClass().getSimpleName(), task, period));
        return this;
    }

    @Override
    public Scheduler build() {
        return new SchedulerImpl(tasks);
    }
}
