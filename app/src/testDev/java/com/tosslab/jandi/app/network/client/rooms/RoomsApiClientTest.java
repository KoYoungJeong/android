package com.tosslab.jandi.app.network.client.rooms;

import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResRoomInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class RoomsApiClientTest {

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(Robolectric.application);
    }

    @Test
    public void testGetRoomInfo() throws Exception {

        List<ResAccountInfo.UserTeam> userTeams = JandiAccountDatabaseManager.getInstance(Robolectric.application).getUserTeams();
        JandiAccountDatabaseManager.getInstance(Robolectric.application).updateSelectedTeam(userTeams.get(0).getTeamId());

        EntityClientManager_ entityClientManager = EntityClientManager_.getInstance_(Robolectric.application);
        ResLeftSideMenu totalEntitiesInfo = entityClientManager.getTotalEntitiesInfo();

        ResRoomInfo roomInfo = RequestApiManager.getInstance().getRoomInfoByRoomsApi(totalEntitiesInfo.team.id, totalEntitiesInfo.team.t_defaultChannelId);

        assertThat(roomInfo, is(notNullValue()));
    }
}