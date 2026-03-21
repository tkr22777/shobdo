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
        }
    }
} 