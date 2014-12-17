package com.tosslab.jandi.app.network;

import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.spring.JandiV2HttpAuthentication;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(RobolectricGradleTestRunner.class)
public class JandiRestClientTest {

    private JandiRestClient jandiRestClient_;

    @Before
    public void setUp() throws Exception {

        jandiRestClient_ = new JandiRestClient_(Robolectric.application);

        ResAccessToken accessToken = getAccessToken();
        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;

    }

    @Test
    public void testGetAccessToken() throws Exception {
        ResAccessToken accessToken = getAccessToken();
        assertNotNull(accessToken);

        System.out.println(accessToken);

    }

    @Test
    public void testGetMyTeam() throws Exception {

        ResMyTeam teamId = jandiRestClient_.getTeamId("mk@tosslab.com");

        assertNotNull(teamId);

        System.out.println(teamId);

    }

    @Test
    public void testGetLeftSideMenu() throws Exception {


        ResMyTeam teamId = jandiRestClient_.getTeamId("mk@tosslab.com");

        System.out.println("========= Get Team Info =========");


        ResLeftSideMenu infosForSideMenu = jandiRestClient_.getInfosForSideMenu(teamId.teamList.get(0).teamId);

        assertNotNull(infosForSideMenu);

        System.out.println(infosForSideMenu);

    }

    @Test
    public void testCreateNewTeam() throws Exception {


        // FIXME 요청 정보에서 누락이 있는지 계속적으로 실패함
        ReqCreateNewTeam reqNewTeam = new ReqCreateNewTeam("Toss Lab, Inc", "testab", "좐수", "john@tosslab.com");
        ResTeamDetailInfo newTeam = null;
        try {
            newTeam = jandiRestClient_.createNewTeam(reqNewTeam);
        } catch (HttpStatusCodeException e) {
            System.out.println(e.getResponseBodyAsString());
            fail(e.getMessage());
        }

        assertNotNull(newTeam);

        System.out.println(newTeam);

    }

    @Test
    public void testSearchFile() throws Exception {


        ReqSearchFile reqSearchFile = new ReqSearchFile();
        reqSearchFile.teamId = 279;
        reqSearchFile.searchType = "file";
        reqSearchFile.writerId = "288";
        reqSearchFile.sharedEntityId = 281;
        reqSearchFile.fileType = "all";
        reqSearchFile.startMessageId = 3601;
        reqSearchFile.listCount = 10;
        reqSearchFile.keyword = "";
        ResSearchFile resSearchFile = jandiRestClient_.searchFile(reqSearchFile);

        assertNotNull(resSearchFile);

        System.out.println(resSearchFile);

    }

    @Test
    public void testGetAccountInfo() throws Exception {


        ResAccountInfo accountInfo = jandiRestClient_.getAccountInfo();

        assertNotNull(accountInfo);

        System.out.println(accountInfo);

    }

    @Test
    public void testSignUp() throws Exception {

        ReqSignUpInfo reqSignUp = new ReqSignUpInfo("test.test@tosslab.com", "123456Ab~", "test test", "en");
        ResAccountInfo resCommon = jandiRestClient_.signUpAccount(reqSignUp);

        assertNotNull(resCommon);

        System.out.println(resCommon);
    }

    @Ignore
    @Test
    public void testActivateAccount() throws Exception {

        // Cannot test.

        ReqAccountActivate reqAccountActivate = new ReqAccountActivate("aa", "aa");
        ResAccountInfo resCommon = jandiRestClient_.activateAccount(reqAccountActivate);

        assertNull(resCommon);

    }


    @Test
    public void testGetMyPendingInvitations() throws Exception {


        List<ResPendingTeamInfo> myPendingInvitations = jandiRestClient_.getMyPendingInvitations();

        assertNotNull(myPendingInvitations);

        System.out.println(myPendingInvitations);
    }

    @Test
    public void testSetMarker() throws Exception {


        ResCommon resCommon = jandiRestClient_.setMarker(281, new ReqSetMarker(279, 12554, ReqSetMarker.CHANNEL));

        assertNotNull(resCommon);

        System.out.println(resCommon);

    }

    @Test
    public void testGetConfig() throws Exception {
        ResConfig config = jandiRestClient_.getConfig();

        assertThat(config, is(notNullValue()));

        System.out.println(config);
    }

    private ResAccessToken getAccessToken() {

        jandiRestClient_.setHeader("Content-Type", "application/json");

        ResAccessToken accessToken = jandiRestClient_.getAccessToken(ReqAccessToken.createPasswordReqToken("mk@tosslab.com", "1234"));
        System.out.println("========= Get Access Token =========");
        return accessToken;
    }
}