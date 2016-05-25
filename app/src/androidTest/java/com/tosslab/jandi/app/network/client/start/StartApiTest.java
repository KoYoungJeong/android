package com.tosslab.jandi.app.network.client.start;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.start.InitializeInfo;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.junit.Before;
import org.junit.Test;

import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;


@org.junit.runner.RunWith(AndroidJUnit4.class)
public class StartApiTest {

    private StartApi startApi;
    private ResAccountInfo accountInfo;

    @Before
    public void setUp() throws Exception {

        ResAccessToken accessToken = new LoginApi(RetrofitBuilder.getInstance())
                .getAccessToken(ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST1_EMAIL, BaseInitUtil.TEST_PASSWORD));
        TokenUtil.saveTokenInfoByPassword(accessToken);
        accountInfo = new AccountApi(RetrofitBuilder.getInstance()).getAccountInfo();
        startApi = new StartApi(RetrofitBuilder.getInstance());
    }

    @Test
    public void testGetInitializeInfo() throws Exception {
        InitializeInfo initializeInfo = startApi.getInitializeInfo(accountInfo.getMemberships().iterator().next().getTeamId());

        assertThat(initializeInfo).isNotNull();
        assertThat(initializeInfo.getTeam()).isNotNull();
        assertThat(initializeInfo.getTopics()).isNotNull().isNotEmpty();
        assertThat(initializeInfo.getChats()).isNotNull().isNotEmpty();
        assertThat(initializeInfo.getFolders()).isNotNull().isNotEmpty();
        assertThat(initializeInfo.getHumans()).isNotNull().isNotEmpty();
        assertThat(initializeInfo.getBots()).isNotNull();
    }
}