package com.github.kagkarlsson.scheduler.example;

import com.github.kagkarlsson.scheduler.MongoScheduler;
import com.github.kagkarlsson.scheduler.MongoSchedulerBuilder;
import com.github.kagkarlsson.scheduler.MongoTaskRepository;
import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.SchedulerBuilder;
import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import com.github.kagkarlsson.scheduler.task.schedule.Schedule;
import com.github.kagkarlsson.scheduler.task.schedule.Schedules;
import com.github.kagkarlsson.scheduler.utils.TestUtils;
import com.github.kagkarlsson.scheduler.utils.TestUtils.MongoTools;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDbSimpleTaskMain {

    private static final Logger LOG = LoggerFactory.getLogger(MongoTaskRepository.class);

    public static void main(String[] args) throws IOException {
        LOG.info("Starting demo app ...");
        MongoTools mongoTools = TestUtils.startEmbeddedMongo();

        // Simple Task
        OneTimeTask<SampleTaskData> oneTimeTask = Tasks.oneTime("one-time", SampleTaskData.class)
            .execute((TaskInstance<SampleTaskData> inst, ExecutionContext ctx) -> LOG.info("Trigger of execution {}, with data {}", ctx.getExecution(), inst));

        Schedule cron = Schedules.cron("*/10 * * * * ?");
        RecurringTask<Void> recurringTask = Tasks.recurring("cron-task", cron)
                .execute((taskInstance, executionContext) -> LOG.info(Instant.now().getEpochSecond() + "s  -  Cron-schedule!"));


        // Instantiation of mongodb based scheduler
        List<Task<?>> knownTasks = new ArrayList<>();
        // Register the one time task to scheduler
        knownTasks.add(oneTimeTask);
        knownTasks.add(recurringTask);

        String databaseName = "scheduler-database";
        String collectionName = "scheduler-collection";
        Scheduler scheduler = MongoScheduler
                .create(mongoTools.getClient(),
                        databaseName,
                        collectionName,
                        knownTasks)
                .pollingInterval(Duration.ofSeconds(1))
                .build();
        // Start scheduler
        scheduler.start();
        // Start one time task
        scheduler.schedule(oneTimeTask.instance("test-instance"),
            Instant.now().plus(10, ChronoUnit.SECONDS));
        scheduler.schedule(recurringTask.schedulableInstance("rec-id"));

        Document task = mongoTools.getClient().getDatabase(databaseName)
            .getCollection(collectionName).find().first();
        LOG.info("Task found {}", task);

        // Proper shutdown of the scheduler
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Received shutdown signal.");
            scheduler.stop();
            mongoTools.getMongodProcess().stop();
        }));

    }

    public static class SampleTaskData {

        private String taskContent;

        public String getTaskContent() {
            return taskContent;
        }

        public void setTaskContent(String taskContent) {
            this.taskContent = taskContent;
        }

        @Override
        public String toString() {
            return "SampleTaskData{" +
                "taskContent='" + taskContent + '\'' +
                '}';
        }
    }
}
