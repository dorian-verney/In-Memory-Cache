import evictionPolicy.LFUPolicy;
import org.junit.jupiter.api.*;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LFUPolicy")
public class LFUPolicyTest {

    private LFUPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new LFUPolicy();
    }

    // -------------------------------------------------------------------------
    // evict() — frequency ordering
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("evict() — frequency ordering")
    class FrequencyOrdering {

        @Test
        @DisplayName("Evicts the only inserted key")
        void evictsSingleKey() {
            policy.onInsert("a");
            assertEquals("a", policy.evict());
        }

        @Test
        @DisplayName("Evicts the least frequently used key")
        void evictsLeastFrequent() {
            policy.onInsert("a"); // freq 1
            policy.onInsert("b"); // freq 1
            policy.onAccess("b"); // b → freq 2
            policy.onAccess("b"); // b → freq 3

            assertEquals("a", policy.evict()); // a has lower freq
        }

        @Test
        @DisplayName("Evicts in ascending frequency order across multiple rounds")
        void evictsAscendingFrequency() {
            policy.onInsert("a"); // freq 1
            policy.onInsert("b"); // freq 1
            policy.onInsert("c"); // freq 1
            policy.onAccess("c"); // c → freq 2
            policy.onAccess("b"); // b → freq 2
            policy.onAccess("c"); // c → freq 3

            // a has freq 1 — must be evicted first
            assertEquals("a", policy.evict());
            // b and c have freq 2 and 3 respectively; b next
            assertEquals("b", policy.evict());
            assertEquals("c", policy.evict());
        }

        @Test
        @DisplayName("After eviction, state is consistent for subsequent evictions")
        void stateConsistentAfterEviction() {
            policy.onInsert("a");
            policy.onInsert("b");
            policy.onAccess("b");

            policy.evict(); // removes a

            // Only b remains; a new insert should be evictable before b
            policy.onInsert("c"); // freq 1
            assertEquals("c", policy.evict()); // c has freq 1, b has freq 2
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
        @DisplayName("Removing the least-frequent key makes the next one evictable")
        void removeLowestFreqKey() {
            policy.onInsert("a");
            policy.onInsert("b");
            policy.onAccess("b");
            policy.onRemove("a");
            assertEquals("b", policy.evict());
        }

        @Test
        @DisplayName("Removing a high-frequency key leaves low-frequency key as next eviction target")
        void removeHighFreqKey() {
            policy.onInsert("a"); // freq 1
            policy.onInsert("b"); // freq 1
            policy.onAccess("b"); // b → freq 2
            policy.onRemove("b");
            assertEquals("a", policy.evict());
        }

        @Test
        @DisplayName("Removing a key that is the only member of its freq bucket cleans up the bucket")
        void removeSoleKeyInBucketCleansUp() {
            policy.onInsert("a");
            policy.onInsert("b");
            policy.onAccess("a"); // a → freq 2, b → freq 1
            policy.onRemove("b"); // remove the sole freq-1 key
            // Only a remains; inserting c should work correctly
            policy.onInsert("c"); // c → freq 1
            assertEquals("c", policy.evict()); // c has lower freq than a
            assertEquals("a", policy.evict());
        }
    }

    // -------------------------------------------------------------------------
    // onAccess() — frequency promotion
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("onAccess() — frequency promotion")
    class FrequencyPromotion {

        @Test
        @DisplayName("Accessing a key promotes it above keys with lower frequency")
        void promotionAboveLowerFreq() {
            policy.onInsert("a");
            policy.onInsert("b");
            policy.onInsert("c");
            policy.onAccess("a"); // a → freq 2
            // b and c are at freq 1, a is at freq 2
            // evict b or c first, then a
            var first = policy.evict();
            assertTrue(Set.of("b", "c").contains(first), "Expected b or c, got " + first);
            var second = policy.evict();
            assertTrue(Set.of("b", "c").contains(second), "Expected b or c, got " + second);
            assertEquals("a", policy.evict());
        }

        @Test
        @DisplayName("Accessing a key multiple times keeps it ahead of singly-accessed keys")
        void multipleAccessesProtectKey() {
            policy.onInsert("rare");
            policy.onInsert("hot");
            for (int i = 0; i < 10; i++) policy.onAccess("hot");

            assertEquals("rare", policy.evict());
            assertEquals("hot", policy.evict());
        }
    }
}
