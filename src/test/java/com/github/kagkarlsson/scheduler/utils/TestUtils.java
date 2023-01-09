package com.github.kagkarlsson.scheduler.utils;

import com.github.kagkarlsson.scheduler.TaskEntity;
import com.github.kagkarlsson.scheduler.task.Execution;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.process.runtime.Network;
import de.flapdoodle.reverse.Transition;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class TestUtils {


    public static class MongoTools {

        MongoClient client;
        RunningMongodProcess mongodProcess;

        public MongoClient getClient() {
            return client;
        }

        public void setClient(MongoClient client) {
            this.client = client;
        }


        public RunningMongodProcess getMongodProcess() {
            return mongodProcess;
        }

        public void setMongodProcess(RunningMongodProcess mongodProcess) {
            this.mongodProcess = mongodProcess;
        }
    }

    public static MongoTools startEmbeddedMongo() throws IOException {
        InetAddress localHost = Network.getLocalHost();
        int freeServerPort = 27017;//Network.freeServerPort(localHost);

        Mongod mongod = new Mongod() {
            @Override
            public Transition<Net> net() {
                return Start.to(Net.class).providedBy(() -> {
                    try {
                        boolean localhostIsIPv6 = Network.localhostIsIPv6();
                        return Net.builder().port(freeServerPort).isIpv6(localhostIsIPv6).build();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        return null;
                    }
                });
            }
        };

        TransitionWalker.ReachedState<RunningMongodProcess> running = mongod.start(Version.Main.V6_0);

        MongoClient mongo = MongoClients
                .create(connectionString(running.current().getServerAddress().toString()));

        MongoTools mongoTools = new MongoTools();
        mongoTools.setClient(mongo);
        mongoTools.setMongodProcess(running.current());

        return mongoTools;
    }

    public static Instant truncateInstant(Instant instant) {
        return instant.truncatedTo(ChronoUnit.MILLIS);
    }

    public static TaskEntity toEntity(Execution in) {

        TaskEntity out = new TaskEntity();
        Optional<TaskInstance<?>> taskInstanceOpt = Optional
                .ofNullable(in.taskInstance);
        taskInstanceOpt.map(TaskInstance::getTaskName).ifPresent(out::setTaskName);
        taskInstanceOpt.map(TaskInstance::getId).ifPresent(out::setTaskInstance);
        taskInstanceOpt
                .map(TaskInstance::getData)
                .map((data) -> data.toString().getBytes(StandardCharsets.UTF_8))
                .ifPresent(out::setTaskData);

        out.setExecutionTime(in.getExecutionTime());
        out.setPicked(in.isPicked());
        out.setPickedBy(in.pickedBy);
        out.setLastFailure(in.lastFailure);
        out.setLastSuccess(in.lastSuccess);
        out.setLastHeartbeat(in.lastHeartbeat);
        out.setVersion(in.version);

        return out;
    }


    private static String connectionString(String url){
        return "mongodb://" + url;
    }
}
