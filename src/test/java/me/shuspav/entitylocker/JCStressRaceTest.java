package me.shuspav.entitylocker;

import jdk.jfr.Description;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@JCStressTest
@Description("concurrent counter increment")
@Outcome(id = "[2]", expect = Expect.ACCEPTABLE, desc = "no update is lost")
public class JCStressRaceTest {

    @State
    public static class TestState {
        Map<Integer, Integer> counters;
        Locker<Integer, Integer> locker;

        public TestState() {
            this.locker = new Locker<>();
            this.counters = new HashMap<>();
            counters.put(0, 0);
        }
    }

    private Consumer<TestState> increment = state -> {
        try {
            state.locker.invoke(0, () -> {
                Integer value = state.counters.get(0);
                state.counters.put(0, value + 1);
                return 0;
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

    @Actor
    public void inc(TestState state) {
        increment.accept(state);
    }

    @Actor
    public void sameInc(TestState state) {
        increment.accept(state);
    }

    @Arbiter
    public void arbiter(TestState state, I_Result result) {
        result.r1 = state.counters.get(0);
    }
}
