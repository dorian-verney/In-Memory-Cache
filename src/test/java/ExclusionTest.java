import org.junit.jupiter.api.DisplayName;
import cache.CacheStore;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertThrows;

import exclusionPolicy.*;

@DisplayName("Exclusion/Eviction Tests")
public class ExclusionTest
{
    // -------------------------------------------------------------------------
    // executeExclusion()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("executeExclusion()")
    class ExecuteExclusion {

        @Test
        @DisplayName("LRU test - least recently used element should be excluded")
        void shouldExcludeLRU() throws InterruptedException {
            CacheStore s = new CacheStore(3);
            s.setExclusionStrategy(new LeastRecentlyUsed());
            s.add("key1", "10", 10_000);
            Thread.sleep(100);
            s.add("key2", "100", 10_000);
            Thread.sleep(100);
            s.add("key3", "1000", 10_000);
            Thread.sleep(100);
            s.get("key1");
            s.add("key4", "10000", 10_000);

            // key2 should be excluded
            assertThrows(NoSuchElementException.class, () -> s.get("key2"));
        }

        @Test
        @DisplayName("LFU test - least frequently used element should be excluded")
        void shouldExcludeLFU() throws InterruptedException {
            CacheStore s = new CacheStore(3);
            s.setExclusionStrategy(new LeastRecentlyUsed());
            s.add("key1", "10", 10_000);
            Thread.sleep(100);
            s.add("key2", "100", 10_000);
            Thread.sleep(100);
            s.add("key3", "1000", 10_000);
            Thread.sleep(100);
            s.get("key1");
            s.add("key4", "10000", 10_000);

            // key2 should be excluded
            assertThrows(NoSuchElementException.class, () -> s.get("key2"));
        }
    }
}
