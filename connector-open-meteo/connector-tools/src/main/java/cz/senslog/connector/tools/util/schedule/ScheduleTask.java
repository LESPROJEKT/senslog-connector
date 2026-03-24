// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.tools.util.schedule;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.SECONDS;

public final class ScheduleTask {

    private static final int DELAY = 2;

    private TaskDescription description;
    private final Runnable task;
    private final long period;

    private ScheduledFuture<?> scheduledTask;

    public ScheduleTask(String name, Runnable task, long period) {
        this.description = new TaskDescription(name, Status.STOPPED);
        this.task = task;
        this.period = period;
    }

    public TaskDescription getDescription() {
        return description;
    }

    public Runnable getTask() {
        return task;
    }

    public long getPeriod() {
        return period;
    }

    public boolean terminate() {
        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            return scheduledTask.cancel(true);
        }
        return true;
    }

    public void schedule(ScheduledExecutorService scheduledService, CountDownLatch latch) {
        scheduledTask = scheduledService.scheduleAtFixedRate(task, DELAY, period, SECONDS);
        description = new TaskDescription(description.getName(), Status.RUNNING);
        new Thread(() -> {
            try {
                scheduledTask.get();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                scheduledTask.cancel(true);
                latch.countDown();
                description = new TaskDescription(description.getName(), Status.STOPPED);
            }
        }, "thread-"+description.getName()).start();
    }
}
