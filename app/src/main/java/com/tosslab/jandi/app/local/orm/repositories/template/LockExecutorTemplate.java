package com.tosslab.jandi.app.local.orm.repositories.template;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockExecutorTemplate {
    private Lock lock;

    public LockExecutorTemplate() {
        lock = new ReentrantLock();
    }

    protected <T> T execute(Executor<T> executor) {
        try {
            lock.lock();
            return executor.execute();
        } finally {
            lock.unlock();
        }
    }

    public interface Executor<T> {
        T execute();
    }

}
