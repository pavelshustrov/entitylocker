//package me.shuspav.entitylocker;
//
//
//import org.junit.jupiter.api.Test;
//import org.openjdk.jmh.annotations.*;
//import org.openjdk.jmh.runner.Runner;
//import org.openjdk.jmh.runner.RunnerException;
//import org.openjdk.jmh.runner.options.Options;
//import org.openjdk.jmh.runner.options.OptionsBuilder;
//import org.openjdk.jmh.runner.options.TimeValue;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Random;
//import java.util.concurrent.TimeUnit;
//
//public class BenchmarksTests {
//    @Test
//    public void benchmarksTest() throws RunnerException {
//        Options opt = new OptionsBuilder()
//                // Specify which benchmarks to run.
//                // You can be more specific if you'd like to run only one benchmark per test.
//                .include(this.getClass().getName() + ".*")
//                // Set the following options as needed
//                .mode (Mode.Throughput)
//                .timeUnit(TimeUnit.MICROSECONDS)
//                .warmupTime(TimeValue.seconds(1))
//                .warmupIterations(20)
//                .measurementTime(TimeValue.seconds(1))
//                .measurementIterations(200)
//                .threads(2)
//                .forks(1)
//                .shouldFailOnError(true)
//                .shouldDoGC(true)
//                //.jvmArgs("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
//                //.addProfiler(WinPerfAsmProfiler.class)
//                .build();
//
//        new Runner(opt).run();
//    }
//
//    @State(Scope.Benchmark)
//    public static class BenchState {
//        Locker<Integer> locker = new Locker<>();
//        Map<Integer, Integer> counters = new HashMap<>();
//        Random rand = new Random();
//    }
//
//    @Benchmark
//    @BenchmarkMode(Mode.All)
//    public void test(BenchState state) {
//        int key = state.rand.nextInt(5);
//        state.locker.invoke(5, () -> {
//            int val = state.counters.getOrDefault(key, 0);
//            state.counters.put(key, val + 1);
//        });
//    }
//}
