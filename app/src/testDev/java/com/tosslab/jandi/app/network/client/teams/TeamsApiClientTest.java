package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.models.ReqCreateAnnouncement;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ReqUpdateAnnouncementStatus;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.spring.JandiV2HttpAuthentication;
import com.tosslab.jandi.app.utils.LanguageUtil;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class TeamsApiClientTest {


    private TeamsApiClient teamsApiClient_;

    @Before
    public void setUp() throws Exception {

        BaseInitUtil.initData(Robolectric.application);

        teamsApiClient_ = new TeamsApiClient_(Robolectric.application);
        teamsApiClient_.setAuthentication(TokenUtil.getRequestAuthentication(Robolectric.application));

    }

    @After
    public void tearDown() throws Exception {
        JandiDatabaseOpenHelper.getInstance(Robolectric.application).getWritableDatabase().close();
    }


    @Ignore
    @Test
    public void testCreateNewTeam() throws Exception {
        ReqCreateNewTeam reqNewTeam = new ReqCreateNewTeam("Toss Lab, Inc2", "testab2");
        ResTeamDetailInfo newTeam = null;
        try {
            newTeam = teamsApiClient_.createNewTeam(reqNewTeam);
        } catch (HttpStatusCodeException e) {
            System.out.println(e.getResponseBodyAsString());
            fail(e.getMessage());
        }

        assertNotNull(newTeam);

        System.out.println(newTeam);
    }

    @Ignore
    @Test
    public void testGetMemberProfile() throws Exception {

        JandiRestClient jandiRestClient_ = new JandiRestClient_(Robolectric.application);
        jandiRestClient_.setAuthentication(TokenUtil.getRequestAuthentication(Robolectric.application));
        ResAccountInfo accountInfo = jandiRestClient_.getAccountInfo();

        ResLeftSideMenu infosForSideMenu = jandiRestClient_.getInfosForSideMenu(accountInfo.getMemberships().get(0).getTeamId());


    }

    @Test
    public void testInviteToTeam() throws Exception {

        JandiRestClient jandiRestClient_ = new JandiRestClient_(Robolectric.application);
        jandiRestClient_.setAuthentication(TokenUtil.getRequestAuthentication(Robolectric.application));
        ResAccountInfo accountInfo = jandiRestClient_.getAccountInfo();

        int teamId = accountInfo.getMemberships().get(0).getTeamId();
        List<ResInvitationMembers> resInvitationMemberses = teamsApiClient_.inviteToTeam(teamId, new ReqInvitationMembers(teamId, Arrays.asList("jsuch2362@naver.com"), LanguageUtil.getLanguage(Robolectric.application)));

        assertThat(resInvitationMemberses, is(notNullValue()));

    }

    @Test
    public void testGetTeamInfo() throws Exception {

        JandiRestClient jandiRestClient_ = new JandiRestClient_(Robolectric.application);
        jandiRestClient_.setAuthentication(TokenUtil.getRequestAuthentication(Robolectric.application));
        ResAccountInfo accountInfo = jandiRestClient_.getAccountInfo();

        int teamId = accountInfo.getMemberships().get(0).getTeamId();

        teamsApiClient_.setAuthentication(TokenUtil.getRequestAuthentication(Robolectric.application));

        ResTeamDetailInfo.InviteTeam teamInfo = teamsApiClient_.getTeamInfo(teamId);

        assertThat(teamInfo, is(notNullValue()));
    }

    @Test
    public void testGetAnnouncement() throws Exception {
        int teamId = 11158788;
        int topicId = 11160305;

        JandiV2HttpAuthentication authentication =
                new JandiV2HttpAuthentication("bearer", "9a275f3e-ee55-42dd-a93d-64197e9e17e6");

        teamsApiClient_.setAuthentication(authentication);

        ResAnnouncement announcement = teamsApiClient_.getAnnouncement(teamId, topicId);

        System.out.println(announcement);

        assertThat(announcement, is(notNullValue()));
    }

    @Test
    public void testCreateAnnouncement() throws Exception {
        int teamId = 11158788;
        int topicId = 11160305;
        int messageId = 361087;
        JandiV2HttpAuthentication authentication =
                new JandiV2HttpAuthentication("bearer", "9a275f3e-ee55-42dd-a93d-64197e9e17e6");
        teamsApiClient_.setAuthentication(authentication);

        ReqCreateAnnouncement reqCreateAnnouncement = new ReqCreateAnnouncement(messageId);
        ResCommon resCommon = teamsApiClient_.createAnnouncement(teamId, topicId, reqCreateAnnouncement);

        System.out.println(resCommon);

        assertThat(resCommon, is(notNullValue()));
    }

    @Test
    public void testUpdateAnnouncementStatus() throws Exception {
        int teamId = 11158788;
        int topicId = 11160305;

        JandiV2HttpAuthentication authentication =
                new JandiV2HttpAuthentication("bearer", "9a275f3e-ee55-42dd-a93d-64197e9e17e6");

        teamsApiClient_.setAuthentication(authentication);

        ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus = new ReqUpdateAnnouncementStatus(topicId, true);
        ResCommon resCommon =
                teamsApiClient_.updateAnnouncementStatus(teamId, 11158789, reqUpdateAnnouncementStatus);

        System.out.println(resCommon);

        assertThat(resCommon, is(notNullValue()));
    }

    @Test
    public void testDeleteAnnouncement() throws Exception {
        int teamId = 11158788;
        int topicId = 11160305;

        JandiV2HttpAuthentication authentication =
                new JandiV2HttpAuthentication("bearer", "9a275f3e-ee55-42dd-a93d-64197e9e17e6");

        teamsApiClient_.setAuthentication(authentication);

        ResCommon resCommon = teamsApiClient_.deleteAnnouncement(teamId, topicId);

        System.out.println(resCommon);

        assertThat(resCommon, is(notNullValue()));
    }
}