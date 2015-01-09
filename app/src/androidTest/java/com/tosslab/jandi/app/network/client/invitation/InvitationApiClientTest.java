package com.tosslab.jandi.app.network.client.invitation;

import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.spring.JandiV2HttpAuthentication;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.shadows.ShadowLog;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricGradleTestRunner.class)
public class InvitationApiClientTest {

    private JandiRestClient jandiRestClient_;
    private InvitationApiClient invitationApiClient;
    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {

        jandiRestClient_ = new JandiRestClient_(Robolectric.application);
        invitationApiClient = new InvitationApiClient_(Robolectric.application);
        ResAccessToken accessToken = getAccessToken();

        jandiRestClient_.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));
        invitationApiClient.setAuthentication(new JandiV2HttpAuthentication(accessToken.getTokenType(), accessToken.getAccessToken()));

        sideMenu = getSideMenu();

        Robolectric.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;

    }

    private ResLeftSideMenu getSideMenu() {
        ResLeftSideMenu infosForSideMenu = jandiRestClient_.getInfosForSideMenu(279);

        return infosForSideMenu;
    }

    private ResAccessToken getAccessToken() {

        jandiRestClient_.setHeader("Content-Type", "application/json");

        ResAccessToken accessToken = jandiRestClient_.getAccessToken(ReqAccessToken.createPasswordReqToken("mk@tosslab.com", "1234"));
        System.out.println("========= Get Access Token =========");
        return accessToken;
    }

    @Test
    public void testInviteTeamMembers() throws Exception {


        List<String> list = new ArrayList<String>();
        list.add("test.test@test.com");
        list.add("john@tosslab.com");

        ReqInvitationMembers invitations = new ReqInvitationMembers(279, list, "ko");
        List<ResInvitationMembers> resInvitationses = invitationApiClient.inviteMembers(invitations);

        assertNotNull(resInvitationses);

        System.out.println(resInvitationses);

    }

    @Test
    public void testGetPendingTeamInfo() {
        List<ResPendingTeamInfo> pedingTeamInfo = invitationApiClient.getPedingTeamInfo();

        assertNotNull(pedingTeamInfo);

        System.out.println(pedingTeamInfo);
    }

}