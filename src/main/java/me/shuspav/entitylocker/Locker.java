package me.shuspav.entitylocker;

import me.shuspav.entitylocker.expections.DeadlockDetectionException;
import me.shuspav.entitylocker.interfaces.EntityLocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/*
    Lock storage used to provide locks for trusted code
 */
public class Locker<ID, R> implements EntityLocker<ID, R> {
    private static Logger log = LoggerFactory.getLogger(Locker.class);
    private ReentrantLock inner = new ReentrantLock();
    private Condition wait = inner.newCondition();
    private ConcurrentMap<ID, ReentrantLock> locks = new ConcurrentHashMap<>();

    private DependencyGraph<ID> dependencies = new DependencyGraph<>();

    private AtomicLong globalLock = new AtomicLong(0);
    private AtomicInteger sharedCount = new AtomicInteger(0);

    @Override
    public R invoke(ID id, Callable<R> code) throws InterruptedException {
        return invoke(id, code, -1, null);
    }

    @Override
    public R invoke(ID id, Callable<R> code, long time, TimeUnit unit) throws InterruptedException {
        try {
            if (!tryLock(id, time, unit)) return null;
            return code.call();
        } catch (DeadlockDetectionException | InterruptedException e) {

            throw new InterruptedException("Deadlock occurred");
        } catch (Exception e) {
            log.error("Exception during running trusted code");
            return null;
        } finally {
            unlock(id);
        }
    }

    @Override
    public R invokeGlobal(Callable<R> code)  {
        inner.lock();
        long threadId = Thread.currentThread().getId();
        try {
            while (!globalLock.compareAndSet(0, threadId) || globalLock.get() != threadId) {
                wait.await();
            }
            while (sharedCount.get() != 0) {
                wait.await();
            }
            return code.call();
        } catch (Exception e) {
            log.error("Exception during running trusted code");
            return null;
        } finally {
            globalLock.compareAndSet(threadId, 0);
            wait.signalAll();
            inner.unlock();
        }
    }

    private void detectDeadlock() throws DeadlockDetectionException {
        if (dependencies.isCycle()) {
            throw new DeadlockDetectionException();
        }
    }

    private boolean tryLock(ID id, long time, TimeUnit unit) throws DeadlockDetectionException, InterruptedException {
        inner.lock();
        try {
            ReentrantLock lock = locks.computeIfAbsent(id, ignore -> new ReentrantLock());

            while (globalLock.get() != 0 || !lock.tryLock()) {
                dependencies.addWait(id);
                detectDeadlock();
                if (time > 0 && unit != null)
                    wait.await(time, unit);
                else
                    wait.await();
            }

            dependencies.addLock(id);
            dependencies.removeWait(id);
            sharedCount.incrementAndGet();
        } finally {
            inner.unlock();
        }
        return true;
    }

    private void unlock(ID id) {
        ReentrantLock lock = locks.get(id);
        if (lock == null || !lock.isHeldByCurrentThread()) return;
        inner.lock();
        try {
            dependencies.removeLock(id);

            lock.unlock();
            sharedCount.decrementAndGet();
            wait.signalAll();
        } finally {
            inner.unlock();
        }
    }
}
