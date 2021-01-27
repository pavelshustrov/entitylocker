package me.shuspav.entitylocker;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DependencyGraph<ID> {
    private ConcurrentMap<ID, Long> lockThread = new ConcurrentHashMap<>();
    private ConcurrentMap<Long, ID> threadLock = new ConcurrentHashMap<>();

    private ReentrantReadWriteLock graphLock = new ReentrantReadWriteLock();

    private ReentrantReadWriteLock.ReadLock readLock = graphLock.readLock();
    private ReentrantReadWriteLock.WriteLock writeLock = graphLock.writeLock();

    /*
        add obtained lock to dependency graph
    */
    public void addLock(ID id) {
        readLock.lock();
        try {
            long threadId = Thread.currentThread().getId();
            lockThread.put(id, threadId);
        } finally {
            readLock.unlock();
        }
    }

    /*
        remove released lock from dependency graph
    */
    public void removeLock(ID id) {
        readLock.lock();
        try {
            long threadId = Thread.currentThread().getId();
            lockThread.remove(id, threadId);
        } finally {
            readLock.unlock();
        }
    }

    /*
      add ID to wait map
    */
    public void addWait(ID id) {
        readLock.lock();
        try {
            long threadId = Thread.currentThread().getId();
            threadLock.put(threadId, id);
        } finally {
            readLock.unlock();
        }
    }

    /*
        remove ID from wait map
     */
    public void removeWait(ID id) {
        readLock.lock();
        try {
            long threadId = Thread.currentThread().getId();
            threadLock.remove(threadId, id);
        } finally {
            readLock.unlock();
        }
    }

    /*
        IsCycle method locks graph for add/remove dependencies and detects cycle for deadlocks
        @return true if there is cycle thus deadlock could occur
        @return false if no deadlocks found
     */
    public boolean isCycle() {
        writeLock.lock();
        try {
            long id = Thread.currentThread().getId();
            Set<Long> visited = new HashSet<>();
            visited.add(id);
            ID dependency = threadLock.get(id);
            while (dependency != null) {
                Long owner = lockThread.get(dependency);
                if (owner == null) return false;
                if (visited.contains(owner)) return true;
                visited.add(owner);
                dependency = threadLock.get(owner);
            }
        } finally {
            writeLock.unlock();
        }
        return false;
    }
}
