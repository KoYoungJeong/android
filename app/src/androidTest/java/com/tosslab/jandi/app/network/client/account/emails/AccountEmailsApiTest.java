package com.tosslab.jandi.app.network.client.account.emails;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(AndroidJUnit4.class)
public class AccountEmailsApiTest {

    private AccountEmailsApi accountEmailsApi;

    @BeforeClass
    public static void setUpClass() throws Exception {
        ResAccessToken accessToken = new LoginApi(RetrofitBuilder.getInstance()).getAccessToken(ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST_EMAIL, BaseInitUtil.TEST_PASSWORD));
        TokenUtil.saveTokenInfoByPassword(accessToken);

    }

    @Before
    public void setUp() throws Exception {
        accountEmailsApi = new AccountEmailsApi(RetrofitBuilder.getInstance());
    }

    @Test
    public void testRequestAddEmail() throws Exception {
        String email = "test.android@tosslab.com";
        ResAccountInfo resAccountInfo = accountEmailsApi.requestAddEmail(new ReqAccountEmail(email));
        assertThat(resAccountInfo.getEmails()).extracting("email")
                .contains(email);
        assertThat(resAccountInfo.getEmails()).extracting("status")
                .contains("pending");

    }
}