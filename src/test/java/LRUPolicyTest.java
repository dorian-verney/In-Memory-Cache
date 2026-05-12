import evictionPolicy.LRUPolicy;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LRUPolicy")
public class LRUPolicyTest {

    private LRUPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new LRUPolicy();
    }

    // -------------------------------------------------------------------------
    // evict() — ordering
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("evict() ordering")
    class EvictOrdering {

        @Test
        @DisplayName("Evicts the only inserted key")
        void evictsSingleKey() {
            policy.onInsert("a");
            assertEquals("a", policy.evict());
        }

        @Test
        @DisplayName("Evicts the least recently inserted key when nothing is accessed")
        void evictsLeastRecentlyInserted() {
            policy.onInsert("a");
            policy.onInsert("b");
            policy.onInsert("c");
            assertEquals("a", policy.evict());
        }

        @Test
        @DisplayName("Accessing a key moves it to most-recently-used, sparing it from eviction")
        void accessedKeyIsNotEvictedFirst() {
            policy.onInsert("a");
            policy.onInsert("b");
            policy.onInsert("c");
            policy.onAccess("a"); // a is now MRU
            // eviction order: b → c → a
            assertEquals("b", policy.evict());
            assertEquals("c", policy.evict());
            assertEquals("a", policy.evict());
        }

        @Test
        @DisplayName("Re-accessing the same key repeatedly keeps it as MRU")
        void repeatedAccessKeepsKeyAsMRU() {
            policy.onInsert("a");
            policy.onInsert("b");
            policy.onAccess("a");
            policy.onAccess("a");
            assertEquals("b", policy.evict());
            assertEquals("a", policy.evict());
        }

        @Test
        @DisplayName("Evicts keys in LRU order across interleaved accesses")
        void lruOrderWithInterleavedAccess() {
            policy.onInsert("a");
            policy.onInsert("b");
            policy.onInsert("c");
            policy.onAccess("b"); // order: a(LRU) → c → b(MRU)
            assertEquals("a", policy.evict());
            assertEquals("c", policy.evict());
            assertEquals("b", policy.evict());
        }
    }

    // -------------------------------------------------------------------------
    // onRemove()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("onRemove()")
    class OnRemove {

        @Test
        @DisplayName("Removed key is not evicted later")
        void removedKeySkippedDuringEviction() {
            policy.onInsert("a");
            policy.onInsert("b");
            policy.onRemove("a");
            assertEquals("b", policy.evict());
        }

        @Test
        @DisplayName("Removing the MRU key makes the next one evictable last")
        void removeMRUKey() {
            policy.onInsert("a");
            policy.onInsert("b");
            policy.onAccess("b"); // b is MRU
            policy.onRemove("b");
            assertEquals("a", policy.evict());
        }
    }
}
