package com.tosslab.jandi.app.local.orm.repositories.template;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockExecutorTemplate {
    private OrmDatabaseHelper helper;

    private Lock lock;

    protected LockExecutorTemplate() {
        lock = new ReentrantLock();
        helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
    }

    protected <T> T execute(Executor<T> executor) {
        try {
            lock.lock();
            return executor.execute();
        } finally {
            lock.unlock();
        }
    }

    protected OrmDatabaseHelper getHelper() {
        return helper;
    }

    public interface Executor<T> {
        T execute();
    }

}
