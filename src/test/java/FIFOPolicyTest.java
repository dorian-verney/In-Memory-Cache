import evictionPolicy.FIFOPolicy;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FIFOPolicy")
public class FIFOPolicyTest {

    private FIFOPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new FIFOPolicy();
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
        @DisplayName("Evicts keys in insertion order regardless of access")
        void evictsInInsertionOrder() {
            policy.onInsert("a");
            policy.onInsert("b");
            policy.onInsert("c");
            assertEquals("a", policy.evict());
            assertEquals("b", policy.evict());
            assertEquals("c", policy.evict());
        }

        @Test
        @DisplayName("onAccess() does not affect eviction order")
        void accessDoesNotChangeOrder() {
            policy.onInsert("a");
            policy.onInsert("b");
            policy.onInsert("c");
            policy.onAccess("a"); // FIFO: should have no effect
            assertEquals("a", policy.evict());
            assertEquals("b", policy.evict());
            assertEquals("c", policy.evict());
        }

        @Test
        @DisplayName("Multiple accesses still preserve insertion order")
        void repeatedAccessPreservesOrder() {
            policy.onInsert("x");
            policy.onInsert("y");
            policy.onAccess("y");
            policy.onAccess("x");
            policy.onAccess("y");
            assertEquals("x", policy.evict());
            assertEquals("y", policy.evict());
        }
    }

    // -------------------------------------------------------------------------
    // onRemove()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("onRemove()")
    class OnRemove {

        @Test
        @DisplayName("Removed key is skipped during eviction")
        void removedKeySkipped() {
            policy.onInsert("a");
            policy.onInsert("b");
            policy.onInsert("c");
            policy.onRemove("a");
            assertEquals("b", policy.evict());
            assertEquals("c", policy.evict());
        }

        @Test
        @DisplayName("Removing a middle key preserves order of remaining keys")
        void removeMiddleKey() {
            policy.onInsert("a");
            policy.onInsert("b");
            policy.onInsert("c");
            policy.onRemove("b");
            assertEquals("a", policy.evict());
            assertEquals("c", policy.evict());
        }
    }
}
