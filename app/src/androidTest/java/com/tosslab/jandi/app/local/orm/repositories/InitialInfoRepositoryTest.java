package com.tosslab.jandi.app.local.orm.repositories;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
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
public class InitialInfoRepositoryTest {
    private ResAccountInfo accountInfo;
    private StartApi startApi;
    private InitializeInfo initializeInfo;
    private long teamId;

    @Before
    public void setUp() throws Exception {
        ResAccessToken accessToken = new LoginApi(RetrofitBuilder.getInstance())
                .getAccessToken(ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST1_EMAIL, BaseInitUtil.TEST_PASSWORD));
        TokenUtil.saveTokenInfoByPassword(accessToken);
        accountInfo = new AccountApi(RetrofitBuilder.getInstance()).getAccountInfo();
        startApi = new StartApi(RetrofitBuilder.getInstance());
        teamId = accountInfo.getMemberships().iterator().next().getTeamId();
        initializeInfo = startApi.getInitializeInfo(teamId);
    }

    @Test
    public void testUpsertInitialInfo() throws Exception {
        boolean success = InitialInfoRepository.getInstance().upsertInitialInfo(initializeInfo);
        assertThat(success).isTrue();

        InitializeInfo initialInfo = InitialInfoRepository.getInstance().getInitialInfo(teamId);

        assertThat(initialInfo).isNotNull();
        assertThat(initialInfo.getTeam()).isNotNull();
        assertThat(initialInfo.getTopics()).isNotNull().isNotEmpty();
        assertThat(initialInfo.getChats()).isNotNull().isNotEmpty();
        assertThat(initialInfo.getFolders()).isNotNull().isNotEmpty();
        assertThat(initialInfo.getHumans()).isNotNull().isNotEmpty();
        assertThat(initialInfo.getBots()).isNotNull();

    }
}