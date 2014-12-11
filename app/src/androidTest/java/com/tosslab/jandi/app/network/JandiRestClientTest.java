package com.tosslab.jandi.app.network;

import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(RobolectricGradleTestRunner.class)
public class JandiRestClientTest {

    private JandiRestClient jandiRestClient_;

    @Before
    public void setUp() throws Exception {

        jandiRestClient_ = new JandiRestClient_(Robolectric.application);


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

        ResAccessToken accessToken = getAccessToken();

        ResMyTeam teamId = jandiRestClient_.getTeamId("mk@tosslab.com");

        System.out.println("========= Get Team Info =========");

        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

        ResLeftSideMenu infosForSideMenu = jandiRestClient_.getInfosForSideMenu(teamId.teamList.get(0).teamId);

        assertNotNull(infosForSideMenu);

        System.out.println(infosForSideMenu);

    }

    @Test
    public void testCreateNewTeam() throws Exception {

        ResAccessToken accessToken = getAccessToken();
        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

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
    public void testCreateTopic() throws Exception {

        ResAccessToken accessToken = getAccessToken();
        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

        ReqCreateTopic reaCreateTopic = new ReqCreateTopic();
        reaCreateTopic.name = "test123123";
        reaCreateTopic.teamId = 279;

        ResCommon result = null;
        try {
            result = jandiRestClient_.createChannel(reaCreateTopic);
        } catch (HttpStatusCodeException e) {
            System.out.println(e.getResponseBodyAsString());
            fail();
        }

        System.out.println(result);

    }

    @Test
    public void testModifyChannelName() throws Exception {

        ResAccessToken accessToken = getAccessToken();
        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

        ReqCreateTopic reaCreateTopic = new ReqCreateTopic();
        reaCreateTopic.name = "test12312443";
        reaCreateTopic.teamId = 279;

        ResCommon resCommon = null;
        try {
            resCommon = jandiRestClient_.modifyChannelName(reaCreateTopic, 6808);
        } catch (HttpStatusCodeException e) {
            System.out.println(e.getResponseBodyAsString());
            fail();
        }

        assertNotNull(resCommon);
    }

    @Test
    public void testDeleteChannel() throws Exception {
        ResAccessToken accessToken = getAccessToken();
        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

        ResLeftSideMenu infosForSideMenu = jandiRestClient_.getInfosForSideMenu(279);

        int myChannelId = 0;
        for (ResLeftSideMenu.Entity entity : infosForSideMenu.entities) {

            if (entity instanceof ResLeftSideMenu.Channel) {
                ResLeftSideMenu.Channel channel = (ResLeftSideMenu.Channel) entity;

                if (channel.ch_creatorId == 285) {
                    myChannelId = channel.id;
                    break;
                }
            }
        }

        if (myChannelId == 0) {
            fail();
        }

        ReqDeleteTopic reqDeleteTopic = new ReqDeleteTopic();
        reqDeleteTopic.teamId = 279;

        ResCommon resCommon = null;
        try {
            resCommon = jandiRestClient_.deleteTopic(myChannelId, reqDeleteTopic);
        } catch (HttpStatusCodeException e) {
            System.out.println(e.getResponseBodyAsString());
            fail();
        }

        assertNotNull(resCommon);

    }

    @Test
    public void testLeave_JoinTopic() throws Exception {

        ResAccessToken accessToken = getAccessToken();
        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

        ResLeftSideMenu infosForSideMenu = jandiRestClient_.getInfosForSideMenu(279);

        int myChannelId = 0;
        for (ResLeftSideMenu.Entity entity : infosForSideMenu.entities) {

            if (entity instanceof ResLeftSideMenu.Channel) {
                ResLeftSideMenu.Channel channel = (ResLeftSideMenu.Channel) entity;

                if (channel.ch_creatorId != infosForSideMenu.user.id) {
                    myChannelId = channel.id;
                    break;
                }
            }
        }

        ReqDeleteTopic reqDeleteTopic = new ReqDeleteTopic();
        reqDeleteTopic.teamId = 279;
        ResCommon resCommon = jandiRestClient_.leaveTopic(myChannelId, reqDeleteTopic);

        assertNotNull("Leave Fail", resCommon);

        ResCommon resCommon1 = jandiRestClient_.joinTopic(myChannelId, reqDeleteTopic);
        assertNotNull("Join Fail", resCommon1);
    }

    @Test
    public void testSearchFile() throws Exception {

        ResAccessToken accessToken = getAccessToken();
        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

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

        ResAccessToken accessToken = getAccessToken();
        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

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

        ResAccessToken accessToken = getAccessToken();
        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

        List<ResPendingTeamInfo> myPendingInvitations = jandiRestClient_.getMyPendingInvitations();

        assertNotNull(myPendingInvitations);

        System.out.println(myPendingInvitations);
    }

    @Test
    public void testInviteTeamMembers() throws Exception {

        ResAccessToken accessToken = getAccessToken();
        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

        List<String> list = new ArrayList<String>();
        list.add("test.test@test.com");
        list.add("john@tosslab.com");

        ReqInvitationMembers invitations = new ReqInvitationMembers(279, list, "ko");
        List<ResInvitationMembers> resInvitationses = jandiRestClient_.inviteMembers(invitations);

        assertNotNull(resInvitationses);

        System.out.println(resInvitationses);

    }

    @Test
    public void testSetMarker() throws Exception {

        ResAccessToken accessToken = getAccessToken();
        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

        ResCommon resCommon = jandiRestClient_.setMarker(281, new ReqSetMarker(279, 12554, ReqSetMarker.CHANNEL));

        assertNotNull(resCommon);

        System.out.println(resCommon);

    }

    private ResAccessToken getAccessToken() {

        jandiRestClient_.setHeader("Content-Type", "application/json");

        ResAccessToken accessToken = jandiRestClient_.getAccessToken(ReqAccessToken.createPasswordReqToken("mk@tosslab.com", "1234"));
        System.out.println("========= Get Access Token =========");
        return accessToken;
    }
}