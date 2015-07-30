package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.network.models.ReqCreateAnnouncement;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ReqUpdateAnnouncementStatus;
import com.tosslab.jandi.app.network.models.ReqUpdateTopicPushSubscribe;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.utils.JandiPreference;
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

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class TeamsApiClientTest {

    @Before
    public void setUp() throws Exception {
        JandiApplication.setContext(Robolectric.application);
        JandiPreference.setAccessTokenType(Robolectric.application, "bearer");
        JandiPreference.setAccessToken(Robolectric.application, "86100b73-18f4-42bd-9f6f-d5535a7b8ce3");

        BaseInitUtil.initData(Robolectric.application);
    }

    @After
    public void tearDown() throws Exception {
        JandiDatabaseOpenHelper.getInstance(Robolectric.application).getWritableDatabase().close();
    }


    @Ignore
    @Test
    public void testCreateNewTeam() throws Exception {

        ReqCreateNewTeam reqNewTeam = new ReqCreateNewTeam("Toss Lab, Inc2", "testab2");

        ResTeamDetailInfo newTeam = RequestApiManager.getInstance().createNewTeamByTeamApi(reqNewTeam);

        assertNotNull(newTeam);

        System.out.println(newTeam);
    }

    @Ignore
    @Test
    public void testGetMemberProfile() throws Exception {

        ResAccountInfo accountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();

        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(accountInfo.getMemberships().get(0).getTeamId());

    }

    @Test
    public void testInviteToTeam() throws Exception {

        ResAccountInfo accountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();

        int teamId = accountInfo.getMemberships().get(0).getTeamId();
        List<ResInvitationMembers> resInvitationMemberses = RequestApiManager.getInstance().inviteToTeamByTeamApi(teamId, new ReqInvitationMembers(teamId, Arrays.asList("jsuch2362@naver.com"), LanguageUtil.getLanguage(Robolectric.application)));

        assertThat(resInvitationMemberses, is(notNullValue()));

    }

    @Test
    public void testGetTeamInfo() throws Exception {

        ResAccountInfo accountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();

        int teamId = accountInfo.getMemberships().get(0).getTeamId();

        ResTeamDetailInfo.InviteTeam teamInfo = RequestApiManager.getInstance().getTeamInfoByTeamApi(teamId);

        assertThat(teamInfo, is(notNullValue()));
    }

    @Ignore
    @Test
    public void testGetAnnouncement() throws Exception {
        int teamId = 11158788;
        int topicId = 11160305;

        ResAnnouncement announcement = RequestApiManager.getInstance().getAnnouncement(teamId, topicId);
        System.out.println(announcement);

        assertThat(announcement, is(notNullValue()));
    }

    @Ignore
    @Test
    public void testCreateAnnouncement() throws Exception {
        int teamId = 11158788;
        int topicId = 11160305;
        int messageId = 361087;

        ReqCreateAnnouncement reqCreateAnnouncement = new ReqCreateAnnouncement(messageId);
        ResCommon resCommon = RequestApiManager.getInstance().createAnnouncement(teamId, topicId, reqCreateAnnouncement);

        System.out.println(resCommon);

        assertThat(resCommon, is(notNullValue()));
    }

    //    @Ignore
    @Test
    public void testUpdateAnnouncementStatus() throws Exception {
        int teamId = 11158788;
        int topicId = 11160305;

        ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus = new ReqUpdateAnnouncementStatus(topicId, true);
        ResCommon resCommon =
                RequestApiManager.getInstance().updateAnnouncementStatus(teamId, 11158789, reqUpdateAnnouncementStatus);

        System.out.println(resCommon);

        assertThat(resCommon, is(notNullValue()));
    }

    @Ignore
    @Test
    public void testDeleteAnnouncement() throws Exception {
        int teamId = 11158788;
        int topicId = 11160305;

        ResCommon resCommon = RequestApiManager.getInstance().deleteAnnouncement(teamId, topicId);

        System.out.println(resCommon);

        assertThat(resCommon, is(notNullValue()));
    }

    @Test
    public void testGetMessage() throws Exception {
        int teamId = 11158788;
        int messageId = 349521;

        ResMessages.OriginalMessage message = RequestApiManager.getInstance().getMessage(teamId, messageId);

        System.out.println(message.toString());

        assertThat(message, is(notNullValue()));
    }

    @Test
    public void testUpdatePushSubscribe() throws Exception {
        int teamId = 279;
        int topicId = 8722;

        ReqUpdateTopicPushSubscribe req = new ReqUpdateTopicPushSubscribe(false);
        ResCommon resCommon = RequestApiManager.getInstance().updateTopicPushSubscribe(teamId, topicId, req);

        System.out.println(resCommon);

        assertThat(resCommon, is(notNullValue()));
    }

}