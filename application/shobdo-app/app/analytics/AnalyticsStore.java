package analytics;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import utilities.ShobdoLogger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AnalyticsStore {

    private static final ShobdoLogger log = new ShobdoLogger(AnalyticsStore.class);
    private static final long TTL_DAYS = 90;

    private final MongoCollection<Document> collection;

    public AnalyticsStore(final MongoCollection<Document> collection) {
        this.collection = collection;
        ensureTtlIndex();
    }

    /**
     * Fire-and-forget async write — never blocks the request thread.
     */
    public void record(final AnalyticsEvent event) {
        CompletableFuture.runAsync(() -> {
            try {
                final Document doc = new Document()
                    .append("event",    event.getEvent())
                    .append("ts",       event.getTs())
                    .append("ip",       event.getIp())
                    .append("word",     event.getWord())
                    .append("referrer", event.getReferrer())
                    .append("status",   event.getStatus());
                collection.insertOne(doc);
            } catch (Exception e) {
                log.error("@AS001 Failed to record analytics event: " + e.getMessage());
            }
        });
    }

    /**
     * TTL index: MongoDB auto-deletes documents older than 90 days.
     */
    private void ensureTtlIndex() {
        try {
            collection.createIndex(
                new Document("ts", 1),
                new IndexOptions().expireAfter(TTL_DAYS, TimeUnit.DAYS)
            );
        } catch (Exception e) {
            log.error("@AS002 Failed to create TTL index on Analytics: " + e.getMessage());
        }
    }
}
