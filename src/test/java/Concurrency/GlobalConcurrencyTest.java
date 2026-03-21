package Concurrency;

import cache.CacheStore;
import commands.CommandDispatcher;
import commands.CommandResponse;
import entry.CacheEntry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;


@Nested
@DisplayName("Global Concurrency tests")
@Execution(ExecutionMode.CONCURRENT)
public class GlobalConcurrencyTest
{
    @Test
    public void testConcurrentClients() throws InterruptedException {
        int clientCount = 10;
        int operationsPerClient = 100;

        ExecutorService executor = Executors.newFixedThreadPool(clientCount);
        CountDownLatch startSignal = new CountDownLatch(1);  // pour synchroniser le départ
        CountDownLatch doneLatch = new CountDownLatch(clientCount); // pour attendre la fin

        CacheStore cache = new CacheStore(100);
        CommandDispatcher dispatcher = new CommandDispatcher(cache);

        AtomicReference<Throwable> threadError = new AtomicReference<>();

        for (int i = 0; i < clientCount; i++) {
            final int clientId = i;
            executor.submit(() -> {
                try {
                    startSignal.await(); // tout le monde attend ici

                    for (int j = 0; j < operationsPerClient; j++) {
                        String key = "client" + clientId + ":key" + j;

                        // simule un vrai flux de commandes
                        CommandResponse res;

                        res = dispatcher.dispatch("ADD " + key + " value" + j + " 5000");
                        //IO.println("ADD client=" + clientId + " op=" + j + " " + res.isSuccess());
                        assertTrue(res.isSuccess());

                        res = dispatcher.dispatch("GET " + key);
                        //IO.println("GET client=" + clientId + " op=" + j + " " + res.isSuccess());
                        assertTrue(res.isSuccess());

                        res = dispatcher.dispatch("SET " + key + " value2" + j + " 5000");
                        //IO.println("SET client=" + clientId + " op=" + j + " " + res.isSuccess());
                        assertTrue(res.isSuccess());

                        res = dispatcher.dispatch("DEL " + key);
                        //IO.println("DEL client=" + clientId + " op=" + j + " " + res.isSuccess());
                        assertTrue(res.isSuccess());

                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Throwable e) {
                    threadError.set(e); // un seul catch, on arrête tout si erreur
                } finally {
                    doneLatch.countDown(); // je signale que j'ai fini
                }
            });
        }

        startSignal.countDown(); // TOP DÉPART — on débloque tous les threads et partent en même temps
        doneLatch.await();       // on attend que tout le monde ait fini
        executor.shutdown();

        // PRINCIPAL THREAD VERIF
        if (threadError.get() != null) {
            throw new AssertionError("Erreur dans un thread enfant", threadError.get());
        }
    }



    @Test
    public void testConcurrentClientsAddGet() throws InterruptedException {
        int clientCount = 10;
        int operationsPerClient = 100;

        ExecutorService executor = Executors.newFixedThreadPool(clientCount);
        CountDownLatch startSignal = new CountDownLatch(1);  // pour synchroniser le départ
        CountDownLatch doneLatch = new CountDownLatch(clientCount); // pour attendre la fin

        CacheStore cache = new CacheStore(100);
        CommandDispatcher dispatcher = new CommandDispatcher(cache);

        AtomicReference<Throwable> threadError = new AtomicReference<>();

        for (int i = 0; i < clientCount; i++) {
            final int clientId = i;
            executor.submit(() -> {
                try {
                    startSignal.await(); // tout le monde attend ici

                    for (int j = 0; j < operationsPerClient; j++) {

                        // simule un vrai flux de commandes
                        dispatcher.dispatch("ADD shared:key" + " value2" + j + " 5000");
                        CommandResponse res = dispatcher.dispatch("GET shared:key");
                        assertTrue(res.isSuccess());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Throwable e) {
                    threadError.set(e); // un seul catch, on arrête tout si erreur
                } finally {
                    doneLatch.countDown(); // je signale que j'ai fini
                }
            });
        }

        startSignal.countDown(); // TOP DÉPART — on débloque tous les threads et partent en même temps
        doneLatch.await();       // on attend que tout le monde ait fini
        executor.shutdown();

        // PRINCIPAL THREAD VERIF
        if (threadError.get() != null) {
            throw new AssertionError("Erreur dans un thread enfant", threadError.get());
        }
    }

    @Test
    public void testAccessCountUnderContention() throws InterruptedException {
        int threadCount = 10;
        int getsPerThread = 100;
        int expectedCount = threadCount * getsPerThread;

        CacheStore cache = new CacheStore(100);
        CommandDispatcher dispatcher = new CommandDispatcher(cache);
        dispatcher.dispatch("ADD shared:key myvalue 99999");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicReference<Throwable> threadError = new AtomicReference<>();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startSignal.await();
                    for (int j = 0; j < getsPerThread; j++) {
                        dispatcher.dispatch("GET shared:key");
                    }
                } catch (Throwable e) {
                    threadError.set(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startSignal.countDown();
        doneLatch.await();
        executor.shutdown();

        if (threadError.get() != null) {
            throw new AssertionError("Erreur thread", threadError.get());
        }

        // vérifie que chaque GET a bien été compté
        CacheEntry entry = cache.getEntry("shared:key");
        assertEquals(expectedCount, entry.getAccessCount());
    }


    @Test
    public void testLastAccessTimeIsMonotonic() throws InterruptedException {
        int threadCount = 10;

        CacheStore cache = new CacheStore(100);
        CommandDispatcher dispatcher = new CommandDispatcher(cache);

        dispatcher.dispatch("ADD shared:key myvalue 99999");

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicReference<Throwable> threadError = new AtomicReference<>();

        // on track le lastAccessTime observé au fil du temps
        AtomicLong lastObserved = new AtomicLong(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startSignal.await();
                    for (int j = 0; j < 100; j++) {
                        long before = System.currentTimeMillis();
                        dispatcher.dispatch("GET shared:key");

                        long lastAccess = cache.getEntry("shared:key").getLastAccessTime();

                        // lastAccess doit être >= before
                        // un autre thread peut avoir fait un GET entre les deux → lastAccess peut être > after
                        // mais il ne peut pas être < before

                        // SEUL INVARIANT
                        assertTrue(lastAccess >= before,
                                "lastAccessTime pas mis à jour : lastAccess=" + lastAccess + " before=" + before);
                    }
                } catch (Throwable e) {
                    threadError.set(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startSignal.countDown();
        doneLatch.await();
        executor.shutdown();

        if (threadError.get() != null) {
            throw new AssertionError("Erreur thread", threadError.get());
        }
    }
}
