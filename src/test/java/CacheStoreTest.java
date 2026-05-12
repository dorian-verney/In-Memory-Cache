import cache.CacheStore;
import evictionPolicy.EvictionPolicy;
import evictionPolicy.LRUPolicy;
import org.junit.jupiter.api.*;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CacheStore")
public class CacheStoreTest {

    private EvictionPolicy policy;
    private CacheStore cache;

    @BeforeEach
    void setUp() {
        policy = new LRUPolicy();
        cache = new CacheStore(policy);
    }

    // -------------------------------------------------------------------------
    // add()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("add()")
    class Add {

        @Test
        @DisplayName("Returns the inserted key")
        void returnsKey() {
            assertEquals("k", cache.add("k", "v", 5_000));
        }

        @Test
        @DisplayName("Inserted value is retrievable via get()")
        void valueRetrievable() {
            cache.add("k", "v", 5_000);
            assertEquals("v", cache.get("k"));
        }

        @Test
        @DisplayName("Duplicate add does not overwrite existing entry")
        void duplicateAddDoesNotOverwrite() {
            cache.add("k", "original", 5_000);
            cache.add("k", "overwrite", 5_000);
            assertEquals("original", cache.get("k"));
        }

        @Test
        @DisplayName("Evicts one entry when capacity is full")
        void evictsWhenFull() {
            var small = new CacheStore(2, new LRUPolicy());
            small.add("a", "1", 5_000);
            small.add("b", "2", 5_000);
            small.add("c", "3", 5_000); // triggers eviction of LRU → "a"

            assertThrows(NoSuchElementException.class, () -> small.get("a"));
            assertEquals("2", small.get("b"));
            assertEquals("3", small.get("c"));
        }
    }

    // -------------------------------------------------------------------------
    // get()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("get()")
    class Get {

        @Test
        @DisplayName("Returns the value for an existing key")
        void returnsValue() {
            cache.add("k", "v", 5_000);
            assertEquals("v", cache.get("k"));
        }

        @Test
        @DisplayName("Throws NoSuchElementException for a missing key")
        void throwsForMissingKey() {
            assertThrows(NoSuchElementException.class, () -> cache.get("missing"));
        }

        @Test
        @DisplayName("Lazy-evicts and throws for an expired entry")
        void lazyEvictsExpiredEntry() throws InterruptedException {
            cache.add("k", "v", 50);
            Thread.sleep(100);
            assertThrows(NoSuchElementException.class, () -> cache.get("k"));
        }

        @Test
        @DisplayName("Expired entry is physically removed after lazy eviction")
        void expiredEntryRemovedFromStore() throws InterruptedException {
            cache.add("k", "v", 50);
            Thread.sleep(100);
            try { cache.get("k"); } catch (NoSuchElementException ignored) {}
            assertNull(cache.getEntry("k"));
        }
    }

    // -------------------------------------------------------------------------
    // set()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("set()")
    class Set {

        @Test
        @DisplayName("Updates value and returns key")
        void updatesValue() {
            cache.add("k", "old", 5_000);
            assertEquals("k", cache.set("k", "new", 5_000));
            assertEquals("new", cache.get("k"));
        }

        @Test
        @DisplayName("Resets TTL so entry is no longer expired")
        void resetsTTL() throws InterruptedException {
            cache.add("k", "v", 50);
            Thread.sleep(60);
            cache.set("k", "refreshed", 5_000); // resets expiry
            assertEquals("refreshed", cache.get("k"));
        }

        @Test
        @DisplayName("Throws NoSuchElementException for a missing key")
        void throwsForMissingKey() {
            assertThrows(NoSuchElementException.class, () -> cache.set("ghost", "v", 5_000));
        }
    }

    // -------------------------------------------------------------------------
    // del()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("del()")
    class Del {

        @Test
        @DisplayName("Removes entry and returns key")
        void removesEntry() {
            cache.add("k", "v", 5_000);
            assertEquals("k", cache.del("k"));
            assertThrows(NoSuchElementException.class, () -> cache.get("k"));
        }

        @Test
        @DisplayName("Throws NoSuchElementException for a missing key")
        void throwsForMissingKey() {
            assertThrows(NoSuchElementException.class, () -> cache.del("ghost"));
        }
    }

    // -------------------------------------------------------------------------
    // evictExpired()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("evictExpired()")
    class EvictExpired {

        @Test
        @DisplayName("Removes expired entries from the provided sample")
        void removesExpiredEntries() throws InterruptedException {
            cache.add("exp1", "a", 50);
            cache.add("exp2", "b", 50);
            cache.add("alive", "c", 5_000);
            Thread.sleep(100);

            cache.evictExpired(java.util.List.of("exp1", "exp2", "alive"));

            assertNull(cache.getEntry("exp1"));
            assertNull(cache.getEntry("exp2"));
            assertNotNull(cache.getEntry("alive"));
        }

        @Test
        @DisplayName("Ignores keys not present in the cache")
        void ignoresMissingKeys() {
            assertDoesNotThrow(() -> cache.evictExpired(java.util.List.of("ghost")));
        }

        @Test
        @DisplayName("Does not remove live entries")
        void keepsLiveEntries() {
            cache.add("k", "v", 5_000);
            cache.evictExpired(java.util.List.of("k"));
            assertNotNull(cache.getEntry("k"));
        }
    }
}
