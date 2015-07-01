package com.tosslab.jandi.app.network.manager.apiexecutor;

import com.tosslab.jandi.app.network.client.account.devices.IAccountDeviceApiAuth;
import com.tosslab.jandi.app.network.manager.RequestApiManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertFalse;

/**
 * Created by Steve SeongUg Jung on 15. 6. 26..
 */
@RunWith(RobolectricGradleTestRunner.class)
public class AwaitablePoolTest {

    private AwaitablePool awaitablePool;

    private List<Integer> list = new CopyOnWriteArrayList<>();

    @Before
    public void setUp() throws Exception {
        awaitablePool = new AwaitablePool(10);

    }

    @Test public void textadasd() throws Exception {
        IAccountDeviceApiAuth iAccountDeviceApiAuth = RequestApiManager.getInstance();
    }

    @Test
    public void testAcquire() throws Exception {

        List<Thread> threads = new ArrayList<>();
        for (int idx = 0; idx < 500; ++idx) {
            Thread thread = new Thread(new MyRunnable(awaitablePool, list, idx));
            thread.start();
            threads.add(thread);
        }

        await().timeout(30000, TimeUnit.MILLISECONDS)
                .until(new Runnable() {
                    @Override
                    public void run() {
                        boolean isAlive = true;
                        while (isAlive) {
                            assertFalse(list.size() > 10);
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            isAlive = false;
                            for (Thread thread : threads) {
                                if (thread.isAlive()) {
                                    isAlive = true;
                                    break;
                                }
                            }
                        }
                    }
                });
    }

    private static class PoolObject {

    }

    private static class MyRunnable implements Runnable {

        private final AwaitablePool awaitablePool;
        private final List<Integer> list;
        private final int idx;

        private MyRunnable(AwaitablePool awaitablePool, List<Integer> list, int idx) {
            this.awaitablePool = awaitablePool;
            this.list = list;
            this.idx = idx;
        }

        @Override
        public void run() {

            Object acquiredValue = awaitablePool.acquire();
            PoolObject acquiredValue1;
            if (acquiredValue == null) {
                acquiredValue1 = new PoolObject();
            } else {
                acquiredValue1 = (PoolObject) acquiredValue;
            }
            list.add(idx);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            awaitablePool.release(acquiredValue1);
            list.remove((Integer) idx);
        }
    }
}