package com.tosslab.jandi.app.network.client.platform;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqUpdatePlatformStatus;
import com.tosslab.jandi.app.network.models.ResCommon;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import static org.junit.Assert.*;

/**
 * Created by tonyjs on 15. 8. 3..
 */
@RunWith(RobolectricGradleTestRunner.class)
public class PlatformApiV2ClientTest {

    @Before
    public void setup() throws Exception {
        BaseInitUtil.initData(Robolectric.application);
    }

    @Test
    public void testUpdatePlatformStatus() throws Exception {
        ReqUpdatePlatformStatus req = new ReqUpdatePlatformStatus(true);

        ResCommon resCommon = RequestApiManager.getInstance().updatePlatformStatus(req);

        assertNotNull(resCommon);
    }
}