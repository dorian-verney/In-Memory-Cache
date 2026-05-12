import cache.CacheStore;
import commands.*;
import evictionPolicy.LRUPolicy;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Commands")
class CommandTest {

    private CacheStore cache;

    @BeforeEach
    void setUp() {
        cache = new CacheStore(new LRUPolicy());
    }

    // =========================================================================
    // ADD
    // =========================================================================

    @Nested
    @DisplayName("AddCommand")
    class Add {

        private final AddCommand cmd = new AddCommand();

        // --- isValid ---------------------------------------------------------

        @Nested
        @DisplayName("isValid()")
        class IsValid {

            @Test
            @DisplayName("Accepts exactly 4 args with a numeric TTL")
            void validArgs() {
                assertTrue(cmd.isValid(new String[]{"ADD", "key", "value", "5000"}));
            }

            @Test
            @DisplayName("Rejects fewer than 4 args")
            void tooFewArgs() {
                assertFalse(cmd.isValid(new String[]{"ADD", "key", "value"}));
                assertFalse(cmd.isValid(new String[]{"ADD", "key"}));
                assertFalse(cmd.isValid(new String[]{"ADD"}));
                assertFalse(cmd.isValid(new String[]{}));
            }

            @Test
            @DisplayName("Rejects more than 4 args")
            void tooManyArgs() {
                assertFalse(cmd.isValid(new String[]{"ADD", "key", "value", "5000", "extra"}));
            }

            @Test
            @DisplayName("Rejects a non-numeric TTL")
            void nonNumericTTL() {
                assertFalse(cmd.isValid(new String[]{"ADD", "key", "value", "abc"}));
            }

            @Test
            @DisplayName("Rejects a float TTL")
            void floatTTL() {
                assertFalse(cmd.isValid(new String[]{"ADD", "key", "value", "1.5"}));
            }

            @Test
            @DisplayName("Accepts a negative TTL (syntactically valid long)")
            void negativeTTL() {
                assertTrue(cmd.isValid(new String[]{"ADD", "key", "value", "-1"}));
            }

            @Test
            @DisplayName("Accepts TTL of zero")
            void zeroTTL() {
                assertTrue(cmd.isValid(new String[]{"ADD", "key", "value", "0"}));
            }

            @Test
            @DisplayName("Accepts empty-string key and value")
            void emptyKeyAndValue() {
                assertTrue(cmd.isValid(new String[]{"ADD", "", "", "5000"}));
            }
        }

        // --- execute ---------------------------------------------------------

        @Nested
        @DisplayName("execute()")
        class Execute {

            @Test
            @DisplayName("Returns a success response containing the key")
            void successResponse() {
                var res = cmd.execute(cache, new String[]{"ADD", "k", "v", "5000"});
                assertTrue(res.isSuccess());
                assertEquals("k", res.getMessage());
            }

            @Test
            @DisplayName("Inserted value is retrievable from the cache")
            void valueStoredInCache() {
                cmd.execute(cache, new String[]{"ADD", "k", "hello", "5000"});
                assertEquals("hello", cache.get("k"));
            }

            @Test
            @DisplayName("Returns error response when args are invalid")
            void invalidArgsReturnsError() {
                var res = cmd.execute(cache, new String[]{"ADD", "k", "v"});
                assertFalse(res.isSuccess());
                assertTrue(res.getMessage().startsWith("ERROR"));
            }

            @Test
            @DisplayName("Duplicate add does not overwrite the existing value")
            void duplicateKeyNotOverwritten() {
                cmd.execute(cache, new String[]{"ADD", "k", "first", "5000"});
                cmd.execute(cache, new String[]{"ADD", "k", "second", "5000"});
                assertEquals("first", cache.get("k"));
            }

            @Test
            @DisplayName("Duplicate add still returns success (putIfAbsent is not an error)")
            void duplicateAddReturnsSuccess() {
                cmd.execute(cache, new String[]{"ADD", "k", "first", "5000"});
                var res = cmd.execute(cache, new String[]{"ADD", "k", "second", "5000"});
                assertTrue(res.isSuccess());
            }

            @Test
            @DisplayName("Multiple distinct keys are all stored independently")
            void multipleDistinctKeys() {
                cmd.execute(cache, new String[]{"ADD", "a", "1", "5000"});
                cmd.execute(cache, new String[]{"ADD", "b", "2", "5000"});
                cmd.execute(cache, new String[]{"ADD", "c", "3", "5000"});
                assertEquals("1", cache.get("a"));
                assertEquals("2", cache.get("b"));
                assertEquals("3", cache.get("c"));
            }

            @Test
            @DisplayName("Entry with TTL 0 is immediately expired")
            void zeroTTLEntryIsImmediatelyExpired() throws InterruptedException {
                cmd.execute(cache, new String[]{"ADD", "k", "v", "0"});
                Thread.sleep(1);
                assertTrue(cache.getEntry("k").isExpired());
            }

            @Test
            @DisplayName("Entry with negative TTL is always expired")
            void negativeTTLEntryIsAlwaysExpired() {
                cmd.execute(cache, new String[]{"ADD", "k", "v", "-1"});
                assertTrue(cache.getEntry("k").isExpired());
            }
        }
    }

    // =========================================================================
    // GET
    // =========================================================================

    @Nested
    @DisplayName("GetCommand")
    class Get {

        private final GetCommand cmd = new GetCommand();

        // --- isValid ---------------------------------------------------------

        @Nested
        @DisplayName("isValid()")
        class IsValid {

            @Test
            @DisplayName("Accepts exactly 2 args")
            void validArgs() {
                assertTrue(cmd.isValid(new String[]{"GET", "key"}));
            }

            @Test
            @DisplayName("Rejects fewer than 2 args")
            void tooFewArgs() {
                assertFalse(cmd.isValid(new String[]{"GET"}));
                assertFalse(cmd.isValid(new String[]{}));
            }

            @Test
            @DisplayName("Rejects more than 2 args")
            void tooManyArgs() {
                assertFalse(cmd.isValid(new String[]{"GET", "key", "extra"}));
            }
        }

        // --- execute ---------------------------------------------------------

        @Nested
        @DisplayName("execute()")
        class Execute {

            @Test
            @DisplayName("Returns success with the correct value for an existing key")
            void returnsValueForExistingKey() {
                cache.add("k", "hello", 5_000);
                var res = cmd.execute(cache, new String[]{"GET", "k"});
                assertTrue(res.isSuccess());
                assertEquals("k", res.getMessage());
                assertTrue(res.getValue().isPresent());
                assertEquals("hello", res.getValue().get());
            }

            @Test
            @DisplayName("Returns error for a missing key")
            void errorForMissingKey() {
                var res = cmd.execute(cache, new String[]{"GET", "ghost"});
                assertFalse(res.isSuccess());
                assertTrue(res.getMessage().startsWith("ERROR"));
            }

            @Test
            @DisplayName("Returns error and getValue() is empty on failure")
            void noValueOnError() {
                var res = cmd.execute(cache, new String[]{"GET", "ghost"});
                assertFalse(res.isSuccess());
                assertFalse(res.getValue().isPresent());
            }

            @Test
            @DisplayName("Returns error for an expired key (lazy eviction)")
            void errorForExpiredKey() throws InterruptedException {
                cache.add("k", "v", 50);
                Thread.sleep(100);
                var res = cmd.execute(cache, new String[]{"GET", "k"});
                assertFalse(res.isSuccess());
                assertTrue(res.getMessage().startsWith("ERROR"));
            }

            @Test
            @DisplayName("Expired key is physically removed after a failed get")
            void expiredKeyRemovedAfterGet() throws InterruptedException {
                cache.add("k", "v", 50);
                Thread.sleep(100);
                cmd.execute(cache, new String[]{"GET", "k"});
                assertNull(cache.getEntry("k"));
            }

            @Test
            @DisplayName("Getting one key does not affect another key's value")
            void getDoesNotSideEffectOtherKeys() {
                cache.add("a", "1", 5_000);
                cache.add("b", "2", 5_000);
                cmd.execute(cache, new String[]{"GET", "a"});
                assertEquals("2", cache.get("b"));
            }

            @Test
            @DisplayName("Returns error response when args are invalid")
            void invalidArgsReturnsError() {
                var res = cmd.execute(cache, new String[]{"GET"});
                assertFalse(res.isSuccess());
                assertTrue(res.getMessage().startsWith("ERROR"));
            }

            @Test
            @DisplayName("Can get a value that was just added via AddCommand")
            void getAfterAdd() {
                new AddCommand().execute(cache, new String[]{"ADD", "k", "v", "5000"});
                var res = cmd.execute(cache, new String[]{"GET", "k"});
                assertTrue(res.isSuccess());
                assertEquals("v", res.getValue().get());
            }
        }
    }

    // =========================================================================
    // SET
    // =========================================================================

    @Nested
    @DisplayName("SetCommand")
    class Set {

        private final SetCommand cmd = new SetCommand();

        // --- isValid ---------------------------------------------------------

        @Nested
        @DisplayName("isValid()")
        class IsValid {

            @Test
            @DisplayName("Accepts exactly 4 args with a numeric TTL")
            void validArgs() {
                assertTrue(cmd.isValid(new String[]{"SET", "key", "value", "5000"}));
            }

            @Test
            @DisplayName("Rejects fewer than 4 args")
            void tooFewArgs() {
                assertFalse(cmd.isValid(new String[]{"SET", "key", "value"}));
                assertFalse(cmd.isValid(new String[]{"SET", "key"}));
                assertFalse(cmd.isValid(new String[]{}));
            }

            @Test
            @DisplayName("Rejects more than 4 args")
            void tooManyArgs() {
                assertFalse(cmd.isValid(new String[]{"SET", "key", "value", "5000", "extra"}));
            }

            @Test
            @DisplayName("Rejects a non-numeric TTL")
            void nonNumericTTL() {
                assertFalse(cmd.isValid(new String[]{"SET", "key", "value", "abc"}));
            }

            @Test
            @DisplayName("Rejects a float TTL")
            void floatTTL() {
                assertFalse(cmd.isValid(new String[]{"SET", "key", "value", "1.5"}));
            }

            @Test
            @DisplayName("Accepts a negative TTL")
            void negativeTTL() {
                assertTrue(cmd.isValid(new String[]{"SET", "key", "value", "-1"}));
            }
        }

        // --- execute ---------------------------------------------------------

        @Nested
        @DisplayName("execute()")
        class Execute {

            @Test
            @DisplayName("Returns success with the key after updating an existing entry")
            void successResponse() {
                cache.add("k", "old", 5_000);
                var res = cmd.execute(cache, new String[]{"SET", "k", "new", "5000"});
                assertTrue(res.isSuccess());
                assertEquals("k", res.getMessage());
            }

            @Test
            @DisplayName("Updated value is visible in the cache")
            void valueUpdatedInCache() {
                cache.add("k", "old", 5_000);
                cmd.execute(cache, new String[]{"SET", "k", "new", "5000"});
                assertEquals("new", cache.get("k"));
            }

            @Test
            @DisplayName("Returns error for a missing key")
            void errorForMissingKey() {
                var res = cmd.execute(cache, new String[]{"SET", "ghost", "v", "5000"});
                assertFalse(res.isSuccess());
                assertTrue(res.getMessage().startsWith("ERROR"));
            }

            @Test
            @DisplayName("Returns error when args are invalid")
            void invalidArgsReturnsError() {
                var res = cmd.execute(cache, new String[]{"SET", "k", "v"});
                assertFalse(res.isSuccess());
                assertTrue(res.getMessage().startsWith("ERROR"));
            }

            @Test
            @DisplayName("Set resets the TTL, keeping an about-to-expire entry alive")
            void resetsTTL() throws InterruptedException {
                cache.add("k", "v", 80);
                Thread.sleep(60); // not expired yet
                cmd.execute(cache, new String[]{"SET", "k", "refreshed", "5000"});
                Thread.sleep(60); // would have expired under old TTL
                assertEquals("refreshed", cache.get("k"));
            }

            @Test
            @DisplayName("Set does not affect other keys in the cache")
            void doesNotAffectOtherKeys() {
                cache.add("a", "1", 5_000);
                cache.add("b", "2", 5_000);
                cmd.execute(cache, new String[]{"SET", "a", "updated", "5000"});
                assertEquals("2", cache.get("b"));
            }

            @Test
            @DisplayName("Successive sets on the same key keep only the last value")
            void successiveSetsSameKey() {
                cache.add("k", "v0", 5_000);
                cmd.execute(cache, new String[]{"SET", "k", "v1", "5000"});
                cmd.execute(cache, new String[]{"SET", "k", "v2", "5000"});
                cmd.execute(cache, new String[]{"SET", "k", "v3", "5000"});
                assertEquals("v3", cache.get("k"));
            }

            @Test
            @DisplayName("Setting on a key deleted beforehand returns error")
            void setAfterDeleteReturnsError() {
                cache.add("k", "v", 5_000);
                cache.del("k");
                var res = cmd.execute(cache, new String[]{"SET", "k", "new", "5000"});
                assertFalse(res.isSuccess());
            }
        }
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    @Nested
    @DisplayName("DeleteCommand")
    class Delete {

        private final DeleteCommand cmd = new DeleteCommand();

        // --- isValid ---------------------------------------------------------

        @Nested
        @DisplayName("isValid()")
        class IsValid {

            @Test
            @DisplayName("Accepts exactly 2 args")
            void validArgs() {
                assertTrue(cmd.isValid(new String[]{"DEL", "key"}));
            }

            @Test
            @DisplayName("Rejects fewer than 2 args")
            void tooFewArgs() {
                assertFalse(cmd.isValid(new String[]{"DEL"}));
                assertFalse(cmd.isValid(new String[]{}));
            }

            @Test
            @DisplayName("Rejects more than 2 args")
            void tooManyArgs() {
                assertFalse(cmd.isValid(new String[]{"DEL", "key", "extra"}));
            }
        }

        // --- execute ---------------------------------------------------------

        @Nested
        @DisplayName("execute()")
        class Execute {

            @Test
            @DisplayName("Returns success with the key after deleting an existing entry")
            void successResponse() {
                cache.add("k", "v", 5_000);
                var res = cmd.execute(cache, new String[]{"DEL", "k"});
                assertTrue(res.isSuccess());
                assertEquals("k", res.getMessage());
            }

            @Test
            @DisplayName("Deleted key is no longer present in the cache")
            void keyRemovedFromCache() {
                cache.add("k", "v", 5_000);
                cmd.execute(cache, new String[]{"DEL", "k"});
                assertNull(cache.getEntry("k"));
            }

            @Test
            @DisplayName("Returns error for a missing key")
            void errorForMissingKey() {
                var res = cmd.execute(cache, new String[]{"DEL", "ghost"});
                assertFalse(res.isSuccess());
                assertTrue(res.getMessage().startsWith("ERROR"));
            }

            @Test
            @DisplayName("Returns error when args are invalid")
            void invalidArgsReturnsError() {
                var res = cmd.execute(cache, new String[]{"DEL"});
                assertFalse(res.isSuccess());
                assertTrue(res.getMessage().startsWith("ERROR"));
            }

            @Test
            @DisplayName("Deleting the same key twice returns error on the second call")
            void doubleDeleteReturnsError() {
                cache.add("k", "v", 5_000);
                cmd.execute(cache, new String[]{"DEL", "k"});
                var res = cmd.execute(cache, new String[]{"DEL", "k"});
                assertFalse(res.isSuccess());
            }

            @Test
            @DisplayName("Deleting one key does not affect other keys")
            void doesNotAffectOtherKeys() {
                cache.add("a", "1", 5_000);
                cache.add("b", "2", 5_000);
                cmd.execute(cache, new String[]{"DEL", "a"});
                assertEquals("2", cache.get("b"));
            }

            @Test
            @DisplayName("A deleted key can be re-added with a new value")
            void deletedKeyCanBeReAdded() {
                cache.add("k", "old", 5_000);
                cmd.execute(cache, new String[]{"DEL", "k"});
                new AddCommand().execute(cache, new String[]{"ADD", "k", "new", "5000"});
                assertEquals("new", cache.get("k"));
            }
        }
    }

    // =========================================================================
    // Full lifecycle integration
    // =========================================================================

    @Nested
    @DisplayName("Lifecycle integration")
    class Lifecycle {

        @Test
        @DisplayName("ADD → GET → SET → GET → DEL → GET full cycle")
        void fullCycle() {
            var add = new AddCommand();
            var get = new GetCommand();
            var set = new SetCommand();
            var del = new DeleteCommand();

            assertTrue(add.execute(cache, new String[]{"ADD", "k", "v1", "5000"}).isSuccess());
            assertEquals("v1", get.execute(cache, new String[]{"GET", "k"}).getValue().get());
            assertTrue(set.execute(cache, new String[]{"SET", "k", "v2", "5000"}).isSuccess());
            assertEquals("v2", get.execute(cache, new String[]{"GET", "k"}).getValue().get());
            assertTrue(del.execute(cache, new String[]{"DEL", "k"}).isSuccess());
            assertFalse(get.execute(cache, new String[]{"GET", "k"}).isSuccess());
        }

        @Test
        @DisplayName("Commands on independent keys do not interfere with each other")
        void independentKeys() {
            var add = new AddCommand();
            var set = new SetCommand();
            var del = new DeleteCommand();
            var get = new GetCommand();

            add.execute(cache, new String[]{"ADD", "a", "1", "5000"});
            add.execute(cache, new String[]{"ADD", "b", "2", "5000"});
            add.execute(cache, new String[]{"ADD", "c", "3", "5000"});

            set.execute(cache, new String[]{"SET", "b", "20", "5000"});
            del.execute(cache, new String[]{"DEL", "c"});

            assertEquals("1",  get.execute(cache, new String[]{"GET", "a"}).getValue().get());
            assertEquals("20", get.execute(cache, new String[]{"GET", "b"}).getValue().get());
            assertFalse(get.execute(cache, new String[]{"GET", "c"}).isSuccess());
        }

        @Test
        @DisplayName("GET after TTL expiry returns error even if SET was called first")
        void getAfterExpiryPostSet() throws InterruptedException {
            var add = new AddCommand();
            var set = new SetCommand();
            var get = new GetCommand();

            add.execute(cache, new String[]{"ADD", "k", "v", "5000"});
            set.execute(cache, new String[]{"SET", "k", "v2", "50"}); // short TTL reset
            Thread.sleep(100);
            assertFalse(get.execute(cache, new String[]{"GET", "k"}).isSuccess());
        }
    }
}
