package com.tosslab.jandi.app.local.database;


import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMyTeam;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import static junit.framework.Assert.assertNotNull;

@RunWith(RobolectricGradleTestRunner.class)
public class JandiAccountDatabaseManagerTest {

    @Before
    public void setUp() throws Exception {

        BaseInitUtil.initData(Robolectric.application);
    }

    @After
    public void tearDown() throws Exception {
        JandiDatabaseOpenHelper.getInstance(Robolectric.application).getWritableDatabase().close();
    }

    @Test
    public void testUpsertLeftSideMenu() throws Exception {

        ResMyTeam teamId = RequestApiManager.getInstance().getTeamIdByMainRest(BaseInitUtil.TEST_ID);
        ResLeftSideMenu infosForSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(teamId.teamList.get(0).teamId);
        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(infosForSideMenu);

        assertNotNull(infosForSideMenu);

    }
}