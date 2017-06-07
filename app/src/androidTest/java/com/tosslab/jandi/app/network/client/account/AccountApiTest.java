package com.tosslab.jandi.app.network.client.account;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;
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
public class AccountApiTest {


    private AccountApi accountApi;

    @BeforeClass
    public static void setUpClass() throws Exception {
        ResAccessToken accessToken = new LoginApi(InnerApiRetrofitBuilder.getInstance()).getAccessToken(ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST_EMAIL, BaseInitUtil.TEST_PASSWORD));
        TokenUtil.saveTokenInfoByPassword(accessToken);
    }

    @Before
    public void setUp() throws Exception {
        accountApi = new AccountApi(InnerApiRetrofitBuilder.getInstance());
    }

    @Test
    public void testGetAccountInfo() throws Exception {
        ResAccountInfo accountInfo = accountApi.getAccountInfo();
        assertThat(accountInfo).isNotNull();
        assertThat(accountInfo.getId().length()).isGreaterThan(0);
    }

    @Test
    public void testUpdatePrimaryEmail() throws Exception {
        ResAccountInfo resAccountInfo = accountApi.updatePrimaryEmail(new ReqUpdatePrimaryEmailInfo(BaseInitUtil.TEST_EMAIL));
        assertThat(resAccountInfo).isNotNull();
        assertThat(resAccountInfo.getEmails().size()).isGreaterThan(0);
        assertThat(resAccountInfo.getEmails()).extracting("id")
                .contains(BaseInitUtil.TEST_EMAIL);
    }
}


























