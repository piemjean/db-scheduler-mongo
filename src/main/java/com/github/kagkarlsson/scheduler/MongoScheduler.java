package com.github.kagkarlsson.scheduler;

import com.github.kagkarlsson.scheduler.logging.LogLevel;
import com.github.kagkarlsson.scheduler.stats.StatsRegistry;
import com.github.kagkarlsson.scheduler.task.OnStartup;
import com.github.kagkarlsson.scheduler.task.Task;
import com.mongodb.client.MongoClient;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class MongoScheduler extends Scheduler {

    protected MongoScheduler(Clock clock, TaskRepository schedulerTaskRepository, TaskRepository clientTaskRepository, TaskResolver taskResolver, int threadpoolSize, ExecutorService executorService, SchedulerName schedulerName, Waiter executeDueWaiter, Duration heartbeatInterval, boolean enableImmediateExecution, StatsRegistry statsRegistry, PollingStrategyConfig pollingStrategyConfig, Duration deleteUnresolvedAfter, Duration shutdownMaxWait, LogLevel logLevel, boolean logStackTrace, List<OnStartup> onStartup) {
        super(clock, schedulerTaskRepository, clientTaskRepository, taskResolver, threadpoolSize,
            executorService, schedulerName, executeDueWaiter, heartbeatInterval,
            enableImmediateExecution,
            statsRegistry, pollingStrategyConfig, deleteUnresolvedAfter, shutdownMaxWait, logLevel, logStackTrace, onStartup);
    }

    public static SchedulerBuilder create(MongoClient mongoClient, String database, Task<?> ... knownTasks) {
        return create(mongoClient, database, Arrays.asList(knownTasks));
    }

    public static MongoSchedulerBuilder create(MongoClient mongoClient, String database,
                                               String collection, Task<?>... knownTasks) {
        return create(mongoClient, database, collection, Arrays.asList(knownTasks));
    }

    public static MongoSchedulerBuilder create(MongoClient mongoClient, String database, List<Task<?>> knownTasks) {
        return new MongoSchedulerBuilder(mongoClient, database, knownTasks);
    }

    public static MongoSchedulerBuilder create(MongoClient mongoClient, String database,
        String collection, List<Task<?>> knownTasks) {
        return new MongoSchedulerBuilder(mongoClient, database, collection, knownTasks);
    }
}
