package me.jellysquid.mods.lithium.common.util.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A ReentrantLock which doesn't do anything.
 */
public class NullReentrantLock extends ReentrantLock {
    public NullReentrantLock() {}

    @Override
    public void lock() {}

    @Override
    public void lockInterruptibly() {}

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) {
        return false;
    }

    @Override
    public void unlock() {}

    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("Lithium does not support this");
    }

    @Override
    public int getHoldCount() {
        return 0;
    }

    @Override
    public boolean isHeldByCurrentThread() {
        return false;
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public boolean hasWaiters(Condition condition) {
        return false;
    }

    @Override
    public int getWaitQueueLength(Condition condition) {
        return 0;
    }
}
