package com.tosslab.jandi.app.network.socket;

import com.jayway.awaitility.Awaitility;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.fail;

@RunWith(RobolectricGradleTestRunner.class)
public class JandiSocketManagerTest {

    private JandiSocketManager socketManager;

    @Before
    public void setUp() throws Exception {
        socketManager = JandiSocketManager.getInstance();
        BaseInitUtil.httpOn();
        BaseInitUtil.logOn();
    }

    @Test
    public void testConnect() throws Exception {

        final boolean[] ok = new boolean[1];
        final boolean[] success = new boolean[1];


        socketManager.connect();

        Awaitility.setDefaultTimeout(15000, TimeUnit.MILLISECONDS);
        Awaitility.await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return ok[0];
            }
        });

        try {

            if (!success[0]) {
                fail("Access Fail");
            }
        } finally {

            socketManager.disconnect();
        }


    }
}