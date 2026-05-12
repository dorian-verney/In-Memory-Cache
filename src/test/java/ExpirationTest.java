import cache.CacheStore;
import evictionPolicy.ExpirationCleaner;
import evictionPolicy.LRUPolicy;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Expiration")
public class ExpirationTest {

    private CacheStore cache;

    @BeforeEach
    void setUp() {
        cache = new CacheStore(new LRUPolicy());
    }

    // -------------------------------------------------------------------------
    // CacheEntry.isExpired() — via CacheStore.getEntry()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("isExpired()")
    class IsExpired {

        @Test
        @DisplayName("A freshly inserted entry is not expired")
        void freshEntryNotExpired() {
            cache.add("k", "v", 5_000);
            assertFalse(cache.getEntry("k").isExpired());
        }

        @Test
        @DisplayName("An entry with TTL 0 expires immediately")
        void zeroTTLExpiresImmediately() throws InterruptedException {
            cache.add("k", "v", 0);
            Thread.sleep(1);
            assertTrue(cache.getEntry("k").isExpired());
        }

        @Test
        @DisplayName("An entry expires after its TTL elapses")
        void entryExpiresAfterTTL() throws InterruptedException {
            cache.add("k", "v", 50);
            Thread.sleep(100);
            assertTrue(cache.getEntry("k").isExpired());
        }

        @Test
        @DisplayName("An entry with a long TTL is not yet expired")
        void longTTLEntryNotExpired() {
            cache.add("k", "v", 60_000);
            assertFalse(cache.getEntry("k").isExpired());
        }
    }

    // -------------------------------------------------------------------------
    // Lazy eviction via get()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Lazy eviction via get()")
    class LazyEviction {

        @Test
        @DisplayName("get() throws and removes an expired entry")
        void getThrowsForExpiredEntry() throws InterruptedException {
            cache.add("k", "v", 50);
            Thread.sleep(100);
            assertThrows(NoSuchElementException.class, () -> cache.get("k"));
            assertNull(cache.getEntry("k"));
        }

        @Test
        @DisplayName("get() succeeds for a live entry with a long TTL")
        void getSucceedsForLiveEntry() {
            cache.add("k", "v", 5_000);
            assertEquals("v", cache.get("k"));
        }

        @Test
        @DisplayName("Mixed entries: live entries survive while expired ones are lazy-evicted")
        void mixedEntriesLazyEviction() throws InterruptedException {
            cache.add("exp", "a", 50);
            cache.add("live", "b", 5_000);
            Thread.sleep(100);

            assertThrows(NoSuchElementException.class, () -> cache.get("exp"));
            assertEquals("b", cache.get("live"));
        }
    }

    // -------------------------------------------------------------------------
    // evictExpired() — active / batch eviction
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("evictExpired()")
    class EvictExpiredBatch {

        @Test
        @DisplayName("Expired entries in the sample are removed")
        void removesExpiredEntriesInSample() throws InterruptedException {
            cache.add("e1", "a", 50);
            cache.add("e2", "b", 50);
            Thread.sleep(100);

            cache.evictExpired(List.of("e1", "e2"));

            assertNull(cache.getEntry("e1"));
            assertNull(cache.getEntry("e2"));
        }

        @Test
        @DisplayName("Live entries in the sample are not removed")
        void keepsLiveEntriesInSample() {
            cache.add("k", "v", 5_000);
            cache.evictExpired(List.of("k"));
            assertNotNull(cache.getEntry("k"));
        }

        @Test
        @DisplayName("Mixed sample: only expired entries are removed")
        void mixedSampleOnlyExpiredRemoved() throws InterruptedException {
            cache.add("exp", "a", 50);
            cache.add("live", "b", 5_000);
            Thread.sleep(100);

            cache.evictExpired(List.of("exp", "live"));

            assertNull(cache.getEntry("exp"));
            assertNotNull(cache.getEntry("live"));
        }

        @Test
        @DisplayName("Sample containing unknown keys does not throw")
        void unknownKeysInSampleDoNotThrow() {
            assertDoesNotThrow(() -> cache.evictExpired(List.of("ghost")));
        }

        @Test
        @DisplayName("Empty sample does not throw")
        void emptySampleDoesNotThrow() {
            assertDoesNotThrow(() -> cache.evictExpired(List.of()));
        }
    }

    // -------------------------------------------------------------------------
    // ExpirationCleaner — background thread
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("ExpirationCleaner background thread")
    class CleanerThread {

        @Test
        @DisplayName("Cleaner removes expired entries within its interval")
        void cleanerRemovesExpiredEntries() throws InterruptedException {
            // Fill the cache so the 20% sample is guaranteed to cover it
            var large = new CacheStore(10, new LRUPolicy());
            for (int i = 0; i < 10; i++) {
                large.add("k" + i, "v", 50); // all expire in 50 ms
            }

            var cleaner = new ExpirationCleaner(large, 100);
            var thread = new Thread(cleaner);
            thread.start();

            Thread.sleep(500); // allow several cleaner cycles
            thread.interrupt();
            thread.join(500);

            long remaining = large.getCache().values().stream()
                    .filter(e -> !e.isExpired())
                    .count();
            assertEquals(0, remaining, "All expired entries should have been cleaned up");
        }

        @Test
        @DisplayName("Cleaner stops cleanly when interrupted")
        void cleanerStopsOnInterrupt() throws InterruptedException {
            var cleaner = new ExpirationCleaner(cache, 200);
            var thread = new Thread(cleaner);
            thread.start();
            thread.interrupt();
            thread.join(500);
            assertFalse(thread.isAlive());
        }

        @Test
        @DisplayName("Cleaner leaves live entries untouched")
        void cleanerPreservesLiveEntries() throws InterruptedException {
            cache.add("live", "v", 10_000);

            var cleaner = new ExpirationCleaner(cache, 50);
            var thread = new Thread(cleaner);
            thread.start();

            Thread.sleep(200);
            thread.interrupt();
            thread.join(500);

            assertNotNull(cache.getEntry("live"));
        }
    }
}
