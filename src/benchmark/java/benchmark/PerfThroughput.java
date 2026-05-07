package benchmark;

import cache.CacheStore;
import commands.CommandDispatcher;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Threads(50)
@Warmup(iterations = 2)
@Measurement(iterations = 3)
public class PerfThroughput
{
    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(PerfThroughput.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @State(Scope.Benchmark) // partagé entre tous les threads
    public static class BenchmarkState
    {
        CacheStore cache;
        CommandDispatcher dispatcher;

        @Setup
        public void setup()
        {
            cache = new CacheStore(10_000);
            dispatcher = new CommandDispatcher(cache);
            // seed toutes les clés possibles avant les benchmarks
            for (int i = 0; i < 1000; i++) {
                dispatcher.dispatch("ADD key:" + i + " value1 10000000");
            }
        }
    }

    // -------
    // Methods to evaluate
    //

    // Blackhole: to AVOID dead code elimination, to avoid JIT Comp. to exec the code
    // because bh consume, the results from get/set is USED

    @Benchmark
    public void benchmarkSet(BenchmarkState state, Blackhole bh)
    {
        // clé unique par thread via ThreadLocalRandom
        String key = "key:" + ThreadLocalRandom.current().nextInt(1000);
        bh.consume(state.dispatcher.dispatch("ADD " + key + " value1 1000000"));
    }

    @Benchmark
    public void benchmarkGet(BenchmarkState state, Blackhole bh)

    {
        bh.consume(state.dispatcher.dispatch("GET key"));
    }
}
