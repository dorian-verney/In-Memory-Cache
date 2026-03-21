import cache.CacheStore;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Cache Store Tests")
public class CacheStoreTest {
    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Default constructor should produce a functional cache")
        void defaultConstructorShouldWork() {
            CacheStore s = new CacheStore();
            s.add("k", "v", 5_000);
            assertEquals("v", s.get("k"));
        }

        @Test
        @DisplayName("Custom capacity constructor should produce a functional cache")
        void customCapacityConstructorShouldWork() {
            CacheStore s = new CacheStore(10);
            s.add("k", "v", 5_000);
            assertEquals("v", s.get("k"));
        }
    }
}

