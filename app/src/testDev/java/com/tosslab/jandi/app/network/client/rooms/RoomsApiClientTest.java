package com.tosslab.jandi.app.network.client.rooms;

import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.client.JandiEntityClient_;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResRoomInfo;
import com.tosslab.jandi.app.utils.TokenUtil;

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

    private RoomsApiClient roomsApiClient;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(Robolectric.application);
        roomsApiClient = new RoomsApiClient_(Robolectric.application);
        roomsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(Robolectric.application));
    }

    @Test
    public void testGetRoomInfo() throws Exception {

        List<ResAccountInfo.UserTeam> userTeams = JandiAccountDatabaseManager.getInstance(Robolectric.application).getUserTeams();
        JandiAccountDatabaseManager.getInstance(Robolectric.application).updateSelectedTeam(userTeams.get(0).getTeamId());

        JandiEntityClient jandiEntityClient = JandiEntityClient_.getInstance_(Robolectric.application);
        ResLeftSideMenu totalEntitiesInfo = jandiEntityClient.getTotalEntitiesInfo();

        ResRoomInfo roomInfo = roomsApiClient.getRoomInfo(totalEntitiesInfo.team.id, totalEntitiesInfo.team.t_defaultChannelId);

        assertThat(roomInfo, is(notNullValue()));
    }
}