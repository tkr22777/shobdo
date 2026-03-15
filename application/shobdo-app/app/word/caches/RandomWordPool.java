package word.caches;

import utilities.ShobdoLogger;
import word.objects.Word;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class RandomWordPool {

    public static final int POOL_SIZE = 2000;
    public static final int BATCH_SIZE = 200;
    public static final long BATCH_SLEEP_MS = 50;

    private final ConcurrentHashMap<Integer, Word> pool = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    private static RandomWordPool instance;
    private static final ShobdoLogger log = new ShobdoLogger(RandomWordPool.class);

    private RandomWordPool() {}

    public static synchronized RandomWordPool getInstance() {
        if (instance == null) {
            instance = new RandomWordPool();
            log.info("@RWP001 Initialized random word pool");
        }
        return instance;
    }

    public void clear() {
        pool.clear();
        counter.set(0);
    }

    public void addWord(final Word word) {
        pool.put(counter.getAndIncrement(), word);
    }

    public Word getRandom() {
        final int size = counter.get();
        if (size == 0) return null;
        return pool.get(ThreadLocalRandom.current().nextInt(size));
    }

    public boolean isReady() {
        return counter.get() > 0;
    }
}
