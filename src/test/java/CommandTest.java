import cache.CacheStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@DisplayName("Command Tests")
class CommandTest {

    private CacheStore cacheStore;

    @BeforeEach
    void setUp() {
        cacheStore = new CacheStore();
    }


    // -------------------------------------------------------------------------
    // Add
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Add")
    class Add {

        @Test
        void shouldAddValue() {
            cacheStore.add("key", "value", 5_000);

            assertEquals("value", cacheStore.get("key"));
        }

        @Test
        void shouldNotOverwriteExistingValue() {
            cacheStore.add("key", "value1", 5_000);
            cacheStore.add("key", "value2", 5_000);

            // putIfAbsent => value1 reste
            assertEquals("value1", cacheStore.get("key"));
        }

        @Test
        void shouldStoreMultipleKeys() {
            cacheStore.add("k1", "v1", 5_000);
            cacheStore.add("k2", "v2", 5_000);

            assertEquals("v1", cacheStore.get("k1"));
            assertEquals("v2", cacheStore.get("k2"));
        }

        @Test
        void shouldReturnKey() {
            String result = cacheStore.add("key", "value", 5_000);
            assertEquals("key", result);
        }
    }


    // -------------------------------------------------------------------------
    // Get
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Get")
    class Get {

        @Test
        void shouldReturnValueWhenKeyExists() {
            cacheStore.add("key", "value", 5_000);

            assertEquals("value", cacheStore.get("key"));
        }

        @Test
        void shouldThrowWhenKeyDoesNotExist() {
            assertThrows(NoSuchElementException.class, () -> cacheStore.get("unknown"));
        }

        @Test
        void shouldNotAffectOtherKeys() {
            cacheStore.add("k1", "v1", 5_000);
            cacheStore.add("k2", "v2", 5_000);

            cacheStore.get("k1");

            assertEquals("v2", cacheStore.get("k2"));
        }

        @Test
        void shouldRecordAccessWithoutError() {
            cacheStore.add("key", "value", 5_000);

            assertDoesNotThrow(() -> cacheStore.get("key"));
        }
    }


    // -------------------------------------------------------------------------
    // Set
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Set")
    class Set {

        @Test
        void shouldUpdateExistingValue() {
            cacheStore.add("key", "value1", 5_000);

            cacheStore.set("key", "value2", 5_000);

            assertEquals("value2", cacheStore.get("key"));
        }

        @Test
        void shouldThrowIfKeyDoesNotExist() {
            assertThrows(NoSuchElementException.class,
                    () -> cacheStore.set("unknown", "value", 5_000));
        }

        @Test
        void shouldReturnKey() {
            cacheStore.add("key", "value", 5_000);

            String result = cacheStore.set("key", "new", 5_000);

            assertEquals("key", result);
        }

        @Test
        void shouldOnlyUpdateTargetKey() {
            cacheStore.add("k1", "v1", 5_000);
            cacheStore.add("k2", "v2", 5_000);

            cacheStore.set("k1", "new", 5_000);

            assertEquals("new", cacheStore.get("k1"));
            assertEquals("v2", cacheStore.get("k2"));
        }
    }


    // -------------------------------------------------------------------------
    // Del
    // -------------------------------------------------------------------------
    @Nested
    @DisplayName("Del")
    class Del {

        @Test
        void shouldDeleteExistingKey() {
            cacheStore.add("key", "value", 5_000);
            cacheStore.del("key");
            assertThrows(NoSuchElementException.class, () -> cacheStore.get("key"));
        }

        @Test
        void shouldThrowIfKeyDoesNotExist() {
            assertThrows(NoSuchElementException.class,
                    () -> cacheStore.del("unknown"));
        }

        @Test
        void shouldDeleteOnlyTargetKey() {
            cacheStore.add("k1", "v1", 5_000);
            cacheStore.add("k2", "v2", 5_000);

            cacheStore.del("k1");

            assertThrows(NoSuchElementException.class, () -> cacheStore.get("k1"));
            assertEquals("v2", cacheStore.get("k2"));
        }

        @Test
        void shouldReturnKey() {
            cacheStore.add("key", "value", 5_000);

            String result = cacheStore.del("key");

            assertEquals("key", result);
        }
    }
}