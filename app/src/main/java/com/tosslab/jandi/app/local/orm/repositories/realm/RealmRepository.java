package com.tosslab.jandi.app.local.orm.repositories.realm;


import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.realm.Realm;

public class RealmRepository {

    private Lock lock;

    protected RealmRepository() {
        lock = new ReentrantLock();
    }

    protected <T> T execute(Executor<T> executor) {
        try {
            lock.lock();
            return executor.execute(Realm.getDefaultInstance());
        } finally {
            lock.unlock();
        }
    }

    public interface Executor<T> {
        T execute(Realm realm);
    }

}
