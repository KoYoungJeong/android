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
        Realm realm = null;
        try {
            lock.lock();
            realm = Realm.getDefaultInstance();
            return executor.execute(realm);
        } finally {
            if (realm != null && !realm.isClosed()) {
                realm.close();
            }
            lock.unlock();
        }
    }

    public interface Executor<T> {
        T execute(Realm realm);
    }

}
