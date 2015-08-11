package com.tosslab.jandi.app.network.client.invitation;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.httpclient.FakeHttp;

import java.util.List;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricGradleTestRunner.class)
public class InvitationApiClientTest {

    //    private JandiRestClient jandiRestClient_;
//    private InvitationApiClient invitationApiClient;
    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {

//        jandiRestClient_ = new JandiRestClient_(RuntimeEnvironment.application);
//        invitationApiClient = new InvitationApiClient_(RuntimeEnvironment.application);
//        ResAccessToken accessToken = getAccessToken();

//        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
//        invitationApiClient.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

        sideMenu = getSideMenu();

        FakeHttp.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;

    }

    private ResLeftSideMenu getSideMenu() {
        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(279);

        return infosForSideMenu;
    }

    private ResAccessToken getAccessToken() {

//        jandiRestClient_.setHeader("Content-Type", "application/json");
//
//        ResAccessToken accessToken = jandiRestClient_.getAccessToken(ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST_ID, BaseInitUtil.TEST_PASSWORD));
        ResAccessToken accessToken = RequestApiManager.getInstance().getAccessTokenByMainRest(ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST_ID, BaseInitUtil.TEST_PASSWORD));
        System.out.println("========= Get Access Token =========");
        return accessToken;
    }

    @Test
    public void testGetPendingTeamInfo() {
//        List<ResPendingTeamInfo> pedingTeamInfo = invitationApiClient.getPedingTeamInfo();
        List<ResPendingTeamInfo> pendingTeamInfos = RequestApiManager.getInstance().getPendingTeamInfoByInvitationApi();
        assertNotNull(pendingTeamInfos);

        System.out.println(pendingTeamInfos);
    }

}