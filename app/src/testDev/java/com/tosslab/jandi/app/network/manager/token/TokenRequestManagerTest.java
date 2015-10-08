package com.tosslab.jandi.app.network.manager.token;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.local.orm.repositories.AccessTokenRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by jsuch2362 on 2015. 10. 8..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class TokenRequestManagerTest {

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Test
    public void testGet() throws Exception {
        String refreshToken = AccessTokenRepository
                .getRepository()
                .getAccessToken()
                .getRefreshToken();

        int count = 5;
        final boolean[] finished = new boolean[count];
        for (int i = 0; i < count; i++) {
            final int finalI = i;
            new Thread(() -> {
                TokenRequestManager.getInstance().get(refreshToken);
                finished[finalI] = true;
            }).start();
        }

        TokenRequestManager.getInstance().get(refreshToken);

        Awaitility.await().until(() -> {
            for (boolean b : finished) {
                if (!b) return false;
            }
            return true;
        });

        TokenRequestManager.getInstance().get(refreshToken);

        assertThat(TokenRequestManager.getInstance().queue.size(), is(equalTo(0)));
    }
}