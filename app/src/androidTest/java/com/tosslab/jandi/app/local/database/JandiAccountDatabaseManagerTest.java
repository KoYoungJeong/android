package com.tosslab.jandi.app.local.database;

import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import static junit.framework.Assert.assertNotNull;

@RunWith(RobolectricGradleTestRunner.class)
public class JandiAccountDatabaseManagerTest {

    private JandiRestClient jandiRestClient;

    @Before
    public void setUp() throws Exception {

        BaseInitUtil.initData(Robolectric.application);
        jandiRestClient = new JandiRestClient_(Robolectric.application);
        jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(Robolectric.application));
    }

    @Test
    public void testUpsertLeftSideMenu() throws Exception {

        ResMyTeam teamId = jandiRestClient.getTeamId("steve@tosslab.com");

        ResLeftSideMenu infosForSideMenu = jandiRestClient.getInfosForSideMenu(teamId.teamList.get(0).teamId);

        JandiEntityDatabaseManager databaseManager = JandiEntityDatabaseManager.getInstance(Robolectric.application);
        databaseManager.upsertLeftSideMenu(infosForSideMenu);

        assertNotNull(infosForSideMenu);

    }
}