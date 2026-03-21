package Concurrency;

public class Others {
}

//
//
//    // -------------------------------------------------------------------------
//    // Concurrency
//    // -------------------------------------------------------------------------
//
//    @Nested
//    @DisplayName("Concurrency")
////    @Execution(ExecutionMode.CONCURRENT)
//    class ConcurrencyTests {
//
//        @Test
//        @DisplayName("Concurrent puts should not corrupt the cache")
//        void concurrentPutsShouldNotCorruptCache() throws InterruptedException {
//            int threadCount = 50;
//            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
//            CountDownLatch latch = new CountDownLatch(threadCount);
//
//            for (int i = 0; i < threadCount; i++) {
//                final int idx = i;
//                executor.submit(() -> {
//                    try {
//                        cacheStore.add("key-" + idx, "value-" + idx, 60_000);
//                    } finally {
//                        latch.countDown();
//                    }
//                });
//            }
//
//            // All threads must be completed before checking with the main thread
//            latch.await(5, TimeUnit.SECONDS);
//            executor.shutdown();
//
//            // All keys must be retrievable without exception
//            for (int i = 0; i < threadCount; i++) {
//                assertEquals("value-" + i, cacheStore.get("key-" + i).getValue());
//            }
//        }
//
//        @Test
//        @DisplayName("Concurrent updateExpiration() calls should not throw")
//        void concurrentUpdateExpirationShouldNotThrow() throws InterruptedException {
//            for (int i = 0; i < 100; i++) {
//                cacheStore.add("key-" + i, "value-" + i, 10);
//            }
//            Thread.sleep(50);
//
//            int threadCount = 10;
//            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
//            CountDownLatch latch = new CountDownLatch(threadCount);
//
//            for (int i = 0; i < threadCount; i++) {
//                executor.submit(() -> {
//                    try {
//                        assertDoesNotThrow(() -> cacheStore.updateExpiration());
//                    } finally {
//                        latch.countDown();
//                    }
//                });
//            }
//
//            latch.await(5, TimeUnit.SECONDS);
//            executor.shutdown();
//        }
//    }
