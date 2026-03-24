// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.util.schedule;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SchedulerImpl implements Scheduler, Runnable {

    private final Set<ScheduleTask> tasks;

    private ScheduledExecutorService scheduler;
    private CountDownLatch latch;

    private Thread schedulerThread;

    public SchedulerImpl(Set<ScheduleTask> tasks) {
        this.tasks = tasks;
    }

    @Override
    public void run() {
        start();
    }

    @Override
    public void start() {

        if (!tasks.isEmpty()) {
            scheduler = Executors.newScheduledThreadPool(tasks.size());
            latch = new CountDownLatch(tasks.size());
            tasks.forEach(t -> t.schedule(scheduler, latch));

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            // TODO no tasks
        }
    }

    @Override
    public void stop() {
        if (getStatus() == Status.RUNNING) {
            scheduler.shutdown();
            scheduler = null;
        }
    }

    @Override
    public Status getStatus() {
        boolean active = scheduler != null && !scheduler.isShutdown();
        return active ? Status.RUNNING : Status.STOPPED;
    }

    @Override
    public Set<TaskDescription> getTaskDescriptions() {
        Set<TaskDescription> descriptions = new HashSet<>(tasks.size());
        for (ScheduleTask task : tasks) {
            descriptions.add(task.getDescription());
        }
        return descriptions;
    }
}
