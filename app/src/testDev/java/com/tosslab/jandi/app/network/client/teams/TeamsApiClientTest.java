package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResInvitationMembers;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.utils.LanguageUtil;

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
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class TeamsApiClientTest {

    @Before
    public void setUp() throws Exception {
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
}