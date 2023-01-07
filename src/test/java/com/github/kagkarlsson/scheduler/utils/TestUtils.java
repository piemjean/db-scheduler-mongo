package com.github.kagkarlsson.scheduler.utils;

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
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
        int freeServerPort = 27017;
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

    private static String connectionString(String url){
        return "mongodb://" + url;
    }
}
