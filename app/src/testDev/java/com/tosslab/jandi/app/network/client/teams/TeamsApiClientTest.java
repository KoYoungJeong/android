package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
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

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

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
        ReqCreateNewTeam reqNewTeam = new ReqCreateNewTeam("Toss Lab, Inc2", "testab2", "좐수", "john@tosslab.com");
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
}