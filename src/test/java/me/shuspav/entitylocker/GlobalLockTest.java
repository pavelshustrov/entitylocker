package me.shuspav.entitylocker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GlobalLockTest {

    private Locker<Integer, Integer> locker;

    @BeforeAll
    public void setUp() {
        locker = new Locker<>();
    }


    @Test
    @Timeout(value = 4, unit = TimeUnit.SECONDS)
    public void globalLock() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            try {
                locker.invoke(1, () -> {
                    try {
                        System.out.println("1 lock start");
                        Thread.sleep(1000);
                        System.out.println("1 lock finish");
                        return 1;
                    } catch (InterruptedException e) {
                        System.out.println("1 lock interrupted");
                        return -1;
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(100);
                locker.invokeGlobal(() -> {
                    try {
                        System.out.println("2 global lock start");
                        Thread.sleep(1000);
                        System.out.println("2 global lock finish");
                        return 2;
                    } catch (InterruptedException e) {
                        System.out.println("2 global lock interrupted");
                        return -1;
                    }
                });
            } catch (InterruptedException e) {
                return;
            }
        });

        Thread t3 = new Thread(() -> {
            try {
                Thread.sleep(300);
                locker.invoke(3, () -> {
                    try {
                        System.out.println("3 lock start");
                        Thread.sleep(1000);
                        System.out.println("3 lock finish");
                        return 3;
                    } catch (InterruptedException e) {
                        System.out.println("3 lock interrupted");
                        return -1;
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
    }


}
