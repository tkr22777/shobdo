package modules;

import akka.actor.ActorSystem;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import play.Configuration;
import common.stores.MongoStoreFactory;
import play.inject.ApplicationLifecycle;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import utilities.ShobdoLogger;
import word.WordLogic;
import word.caches.WordCache;
import word.stores.WordStoreMongoImpl;

import javax.inject.Singleton;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * This module handles scheduled/periodic tasks in the application.
 * It uses Akka's scheduler to run tasks at specified intervals.
 */
public class SchedulerModule extends AbstractModule {

    private static final ShobdoLogger log = new ShobdoLogger(SchedulerModule.class);

    @Override
    protected void configure() {
        bind(TaskScheduler.class).asEagerSingleton();
    }

    /**
     * Singleton class that manages the periodic task.
     */
    @Singleton
    public static class TaskScheduler {

        private final ActorSystem actorSystem;
        private final ExecutionContext executionContext;
        private static final String DISABLE_PING_ENV = "DISABLE_PERIODIC_PING";

        @Inject
        public TaskScheduler(ActorSystem actorSystem,
                            ExecutionContext executionContext,
                            ApplicationLifecycle lifecycle,
                            Configuration config) {
            this.actorSystem = actorSystem;
            this.executionContext = executionContext;

            // Register stop hook first so it always fires
            lifecycle.addStopHook(() -> {
                log.debug("Shutting down scheduled tasks");
                return CompletableFuture.completedFuture(null);
            });

            // Skip all scheduling when disabled (e.g. during tests)
            if (!config.getBoolean("shobdo.scheduler.enabled", true)) {
                log.debug("SchedulerModule disabled via shobdo.scheduler.enabled=false — skipping all tasks");
                return;
            }

            final WordLogic wordLogic = new WordLogic(
                new WordStoreMongoImpl(
                    MongoStoreFactory.getWordCollection(),
                    MongoStoreFactory.getInflectionIndexCollection()
                ),
                WordCache.getCache()
            );

            // Async startup fill — non-blocking, pool stays empty until done
            CompletableFuture.runAsync(() -> wordLogic.fillRandomWordPool());

            // Periodic refresh every 2 hours
            actorSystem.scheduler().schedule(
                Duration.create(2, TimeUnit.HOURS),
                Duration.create(2, TimeUnit.HOURS),
                () -> wordLogic.fillRandomWordPool(),
                executionContext
            );

            // Check if pinging is disabled via environment variable
            boolean pingDisabled = Boolean.parseBoolean(System.getenv(DISABLE_PING_ENV));

            if (pingDisabled) {
                log.info("Periodic ping task is disabled via " + DISABLE_PING_ENV + " environment variable");
            } else {
                // Schedule the periodic task only if not disabled
                this.schedulePeriodicTask();
            }

        }

        /**
         * Periodic task: ping render domains to keep them alive
         */
        private void schedulePeriodicTask() {
            // Run task every 10 seconds
            actorSystem.scheduler().schedule(
                Duration.create(10, TimeUnit.SECONDS),    // Start after 10 seconds
                Duration.create(10, TimeUnit.SECONDS),    // Run every 10 seconds
                () -> {
                    pingUrl("https://www.shobdo.info");
                    WordCache.getCache().printCacheInfo();
                },
                executionContext
            );
        }

        private void pingUrl(String urlStr) {
            try {
                log.info("Pinging URL: " + urlStr);
                
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                int responseCode = connection.getResponseCode();
                connection.disconnect();
                
                if (responseCode >= 200 && responseCode < 300) {
                    log.info("Successfully pinged " + urlStr + ", response code: " + responseCode);
                } else {
                    log.error("Failed to ping " + urlStr + ", response code: " + responseCode);
                }
            } catch (IOException e) {
                log.error("Error pinging " + urlStr + ": " + e.getMessage(), e);
            }
        }
    }
} 