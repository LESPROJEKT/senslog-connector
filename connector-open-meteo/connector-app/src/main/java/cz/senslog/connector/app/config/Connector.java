// Copyright (c) 2026 UWB & LESP.
// The UWB & LESP license this file to you under the BSD-3-Clause license.

package cz.senslog.connector.app.config;

import cz.senslog.connector.tools.exception.ModuleInterruptedException;
import cz.senslog.connector.fetch.api.ExecutableFetcher;
import cz.senslog.connector.model.api.AbstractModel;
import cz.senslog.connector.model.api.Converter;
import cz.senslog.connector.push.api.ConnectorPusher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;

import static cz.senslog.connector.tools.util.Pipeline.of;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * The class {@code Connector} represents a created connector
 * which allows to be scheduled by defined period.
 * The idea is to wrap functionality of a connector. The flow is
 * defined as 'fetcher' -> 'converter' -> 'pusher'.
 *
 * @author Lukas Cerny
 * @version 1.0
 * @since 1.0
 */
public final class Connector {

    private static final Logger logger = LogManager.getLogger(Connector.class);

    /** Default initialization delay value when the scheduler starts to schedule tasks (in millis). */
    private static final int DEFAULT_DELAY_MILLIS = 2_000; // 2s

    /** Default value for scheduling tasks when the value missing in the configuration file (in millis). */
    private static final int DEFAULT_SCHEDULE_PERIOD_MILLIS = 3_600_000;  // every hour

    /** Name of the connector */
    private final String name;

    /** Instance of a fetcher that provides data. */
    private final ExecutableFetcher<? super AbstractModel> fetcherExecutor;

    /** Instance of a pusher that receives data. */
    private final ConnectorPusher<? super AbstractModel> pusher;

    /** Converter between fetch and push. */
    private final Converter<? super AbstractModel, ? super AbstractModel> converter;

    /** Period for scheduler. */
    private final Integer period;

    /** Initialization delay for scheduler. */
    private final Integer initDelay;

    private final LocalTime startAt;

    /**
     * Constructor allows to set all attributes.
     * @param name - name of the connector.
     * @param fetcherExecutor - instance of fetcher.
     * @param pusher - instance of pusher.
     * @param converter - instance of converter.
     * @param period - period for scheduling.
     */
    public Connector(
            String name,
            ExecutableFetcher<? super AbstractModel> fetcherExecutor,
            ConnectorPusher<? super AbstractModel> pusher,
            Converter<? super AbstractModel, ? super AbstractModel> converter,
            Integer period,
            Integer initDelay,
            LocalTime startAt
    ) {
        this.name = name;
        this.fetcherExecutor = fetcherExecutor;
        this.pusher = pusher;
        this.converter = converter;
        this.period = period != null ? period * 1_000 : null; // to millis
        this.initDelay = initDelay != null ? initDelay * 1_000 : null; // to millis
        this.startAt = startAt;
    }

    /**
     * Returns name of the connector.
     * @return name of the connector.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns period for scheduler.
     * @return period for scheduler.
     */
    public Optional<Integer> getPeriod() {
        return ofNullable(period);
    }

    /**
     * Returns initialization delay for scheduler.
     * @return delay for scheduler.
     */
    public Optional<Integer> getInitDelay() {
        return ofNullable(initDelay);
    }

    /**
     * Returns time to schedule the connector at.
     * @return time to execute the connector
     */
    public Optional<LocalTime> getStartAt() {
        return ofNullable(startAt);
    }

    /**
     * Returns scheduling runnable task of the connector flow.
     * @return runnable task
     */
    public Runnable getTask() {
        return () -> of(fetcherExecutor::execute).pipe(converter::convert).end(pusher::push);
    }

    /**
     * Schedules connector according to settings. Input parameters are scheduled service
     * {@link ScheduledExecutorService} and {@link CountDownLatch} uses as a thread barrier.
     * @param scheduledService - scheduled service
     * @param latch - thread counter
     */
    public void schedule(ScheduledExecutorService scheduledService, CountDownLatch latch) {

        long schedulePeriod = getPeriod().orElse(DEFAULT_SCHEDULE_PERIOD_MILLIS);
        LocalTime startAtTime = getStartAt().orElse(null);
        Runnable task = getTask();

        StringBuilder logScheduling = new StringBuilder("Scheduling the '"+getName()+"' starts");
        long delay = DEFAULT_DELAY_MILLIS;
        if (startAtTime != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDate startAtDate = now.toLocalTime().isBefore(startAtTime) ? now.toLocalDate() : now.toLocalDate().plusDays(1);
            LocalDateTime startAt = LocalDateTime.of(startAtDate, startAtTime);
            logScheduling.append(" at ").append(startAt);
            delay = now.until(startAt, ChronoUnit.MILLIS);
            if (delay <= 0) {
                delay = DEFAULT_DELAY_MILLIS;
            }
        } else if (getInitDelay().isPresent()) {
            delay = getInitDelay().get();
            logScheduling.append(" in ").append(delay);
        }
        logScheduling.append(" with the period ").append(schedulePeriod).append(" milliseconds.");

        logger.info(logScheduling);
        ScheduledFuture<?> future = scheduledService.scheduleAtFixedRate(task, delay, schedulePeriod, MILLISECONDS);

        new Thread(() -> {
            try {
                future.get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof ModuleInterruptedException) {
                    logger.warn(e.getMessage());
                } else {
                    logger.catching(e);
                }
            } catch (Exception e) {
                logger.catching(e);
            } finally {
                future.cancel(true);
                latch.countDown();
            }
        }, "thread-"+getName()).start();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connector connector = (Connector) o;
        return Objects.equals(name, connector.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}