package com.tosslab.jandi.app.network.client.account.password;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ResCommon;

import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(AndroidJUnit4.class)
public class AccountPasswordApiTest {

    @Test
    public void testResetPassword() throws Exception {
        ResCommon resCommon = new AccountPasswordApi(InnerApiRetrofitBuilder.getInstance()).resetPassword(new ReqAccountEmail(BaseInitUtil.TEST_EMAIL));
        assertThat(resCommon).isNotNull();
    }
}