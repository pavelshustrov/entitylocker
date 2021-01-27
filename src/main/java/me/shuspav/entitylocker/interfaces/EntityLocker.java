package me.shuspav.entitylocker.interfaces;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public interface EntityLocker<ID, R> {
    R invoke(ID id, Callable<R> code) throws InterruptedException;

    R invoke(ID id, Callable<R> code, long time, TimeUnit unit) throws InterruptedException;

    R invokeGlobal(Callable<R> code);
}
