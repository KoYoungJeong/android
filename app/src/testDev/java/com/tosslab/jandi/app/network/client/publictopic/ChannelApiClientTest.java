package com.tosslab.jandi.app.network.client.publictopic;

import com.tosslab.jandi.app.local.database.JandiDatabaseOpenHelper;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

@RunWith(RobolectricGradleTestRunner.class)
public class ChannelApiClientTest {

    private ResLeftSideMenu sideMenu;

    @Before
    public void setUp() throws Exception {

        BaseInitUtil.initData(Robolectric.application);
        sideMenu = getSideMenu();

    }

    @After
    public void tearDown() throws Exception {
        JandiDatabaseOpenHelper.getInstance(Robolectric.application).getWritableDatabase().close();
    }


    private ResLeftSideMenu getSideMenu() {

        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance()
                .getInfosForSideMenuByMainRest(JandiAccountDatabaseManager.getInstance(Robolectric.application).getUserTeams().get(0).getTeamId());

        return infosForSideMenu;
    }

}