package com.tosslab.jandi.app.network.manager.ApiExecutor;

import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tee on 15. 6. 23..
 */

public class AwaitablePool<T> {

    private final Object[] mPool;
    int i, j = 0;
    private int mAcquireRequestCnt = 0;
    private int mPoolSize;
    private boolean mMaxCntOfObjectAcquired = false;

    public AwaitablePool(int maxPoolSize) {
        if (maxPoolSize <= 0) {
            throw new IllegalArgumentException("The max pool size must be > 0");
        }
        mPool = new Object[maxPoolSize];
    }

    @SuppressWarnings("unchecked")
    public synchronized T acquire() {
        if (!mMaxCntOfObjectAcquired) {
            if (mAcquireRequestCnt >= mPool.length) {
                mMaxCntOfObjectAcquired = true;
            } else {
                mAcquireRequestCnt++;
            }
        }
        if (mMaxCntOfObjectAcquired) {
            while (mPoolSize <= 0) {
                try {
                    this.wait();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            LogUtil.e("acquire : " + i++);
            return getReturnInstance();
        } else {
            LogUtil.e("acquire : " + i++);
            if (mPoolSize > 0) {
                return getReturnInstance();
            }
            return null;
        }
    }

    private T getReturnInstance() {
        final int lastPooledIndex = mPoolSize - 1;
        T instance = (T) mPool[lastPooledIndex];
        mPool[lastPooledIndex] = null;
        --mPoolSize;
        return instance;
    }

    public boolean release(T instance) {
        LogUtil.e("release : " + j++);
        if (isInPool(instance)) {
            throw new IllegalStateException("Already in the pool!");
        }
        if (mPoolSize < mPool.length) {
            mPool[mPoolSize] = instance;
            mPoolSize++;
            if (mMaxCntOfObjectAcquired) {
                synchronized (this) {
                    this.notifyAll();
                }
            }
            return true;
        }
        return false;
    }

    private boolean isInPool(T instance) {
        for (int i = 0; i < mPoolSize; i++) {
            if (mPool[i] == instance) {
                return true;
            }
        }
        return false;
    }
}
