import cache.CacheStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Expiration Tests")
public class ExpirationTest
{
    private CacheStore cacheStore;

    @BeforeEach
    void setUp() {
        cacheStore = new CacheStore();
    }

    // -------------------------------------------------------------------------
    // isExpired()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("isExpired()")
    class Expiration {

        @Test
        @DisplayName("A freshly inserted entry should not be expired")
        void freshEntryShouldNotBeExpired() {
            cacheStore.add("fresh", "value", 60_000);
            assertFalse(cacheStore.getStorage().get("fresh").isExpired());
        }

        @Test
        @DisplayName("An entry with TTL 0 should be immediately expired")
        void zeroTtlShouldBeExpired() throws InterruptedException {
            cacheStore.add("zero", "value", 0);
            Thread.sleep(1); // ensure at least 1 ms has elapsed
            assertTrue(cacheStore.getStorage().get("zero").isExpired());
        }

        @Test
        @DisplayName("An entry should be expired after its TTL elapses")
        void entryShouldExpireAfterTTL() throws InterruptedException {
            cacheStore.add("short", "value", 50);
            Thread.sleep(100); // wait longer than TTL
            assertTrue(cacheStore.getStorage().get("short").isExpired());
        }

        @Test
        @DisplayName("An entry should not be expired before its TTL elapses")
        void entryShouldNotExpireBeforeTTL() {
            cacheStore.add("long", "value", 60_000);
            assertFalse(cacheStore.getStorage().get("long").isExpired());
        }
    }


    // -------------------------------------------------------------------------
    // updateExpiration()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("updateExpiration()")
    class UpdateExpiration {

        @Test
        @DisplayName("Expired entries should be evicted by updateExpiration()")
        void shouldEvictExpiredEntries() throws InterruptedException {
            cacheStore.add("expired", "value", 50);
            Thread.sleep(100);
            cacheStore.updateExpiration();
            assertThrows(NoSuchElementException.class, () -> cacheStore.get("expired"));
        }

        @Test
        @DisplayName("Valid entries should survive updateExpiration()")
        void shouldKeepValidEntries() throws InterruptedException {
            cacheStore.add("alive", "value", 60_000);
            Thread.sleep(10);
            cacheStore.updateExpiration();
            assertEquals("value", cacheStore.get("alive"));
        }

        @Test
        @DisplayName("Mixed entries: only expired ones should be removed")
        void shouldEvictOnlyExpiredEntries() throws InterruptedException {
            cacheStore.add("expired1", "a", 50);
            cacheStore.add("expired2", "b", 50);
            cacheStore.add("alive",    "c", 60_000);

            Thread.sleep(100);
            cacheStore.updateExpiration();

            assertThrows(NoSuchElementException.class, () -> cacheStore.get("expired1"));
            assertThrows(NoSuchElementException.class, () -> cacheStore.get("expired2"));
            assertEquals("c", cacheStore.get("alive"));
        }

        @Test
        @DisplayName("updateExpiration() on an empty cache should not throw")
        void shouldHandleEmptyCache() {
            assertDoesNotThrow(() -> cacheStore.updateExpiration());
        }
    }
}
