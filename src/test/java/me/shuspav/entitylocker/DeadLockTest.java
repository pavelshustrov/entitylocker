package me.shuspav.entitylocker;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DeadLockTest {
    private static Logger log = LoggerFactory.getLogger(DeadLockTest.class);

    private Locker<Integer, Integer> locker;

    @BeforeAll
    public void setUp() {
        locker = new Locker<>();
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    public void DoubleThreadDeadlock() throws InterruptedException {


        Thread t1 = new Thread(() -> {
            try {
                locker.invoke(1, () -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        return null;
                    }
                    try {
                        return locker.invoke(2, () -> {
                            log.info("running {}", Thread.currentThread().getId());
                            return 2;
                        });
                    } catch (InterruptedException e) {
                        log.info("Deadlock {}", Thread.currentThread().getId());
                        return null;
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                locker.invoke(2, () -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        return -1;
                    }
                    try {
                        locker.invoke(1, () -> {
                            log.info("running {}", Thread.currentThread().getId());
                            return 1;
                        });
                    } catch (InterruptedException e) {
                        log.info("Deadlock {}", Thread.currentThread().getId());
                        return null;
                    }
                    return 2;
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }

    @Test
    @Timeout(value = 4, unit = TimeUnit.SECONDS)
    public void TripleThreadDeadlock() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            try {
                locker.invoke(1, () -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        return null;
                    }
                    try {
                        return locker.invoke(2, () -> {
                            System.out.println(Thread.currentThread().getName());
                            return 2;
                        });
                    } catch (InterruptedException e) {
                        System.out.println("Deadlock " + Thread.currentThread().getName());
                        return null;
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                locker.invoke(2, () -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        return null;
                    }
                    try {
                        return locker.invoke(3, () -> {
                            System.out.println(Thread.currentThread().getName());
                            return 3;
                        });
                    } catch (InterruptedException e) {
                        System.out.println("Deadlock " + Thread.currentThread().getName());
                        return null;
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread t3 = new Thread(() -> {
            try {
                locker.invoke(3, () -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        return null;
                    }
                    try {
                        return locker.invoke(1, () -> {
                            System.out.println(Thread.currentThread().getName());
                            return 1;
                        });
                    } catch (InterruptedException e) {
                        System.out.println("Deadlock " + Thread.currentThread().getName());
                        return null;
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
