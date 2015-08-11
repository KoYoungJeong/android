package com.tosslab.jandi.app.network.client.rooms;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResRoomInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(JandiRobolectricGradleTestRunner.class)
public class RoomsApiClientTest {

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(RuntimeEnvironment.application);
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();


    }

    @Test
    public void testGetRoomInfo() throws Exception {

        List<ResAccountInfo.UserTeam> userTeams = AccountRepository.getRepository().getAccountTeams();
        AccountRepository.getRepository().updateSelectedTeamInfo(userTeams.get(0).getTeamId());

        EntityClientManager_ entityClientManager = EntityClientManager_.getInstance_(RuntimeEnvironment.application);
        ResLeftSideMenu totalEntitiesInfo = entityClientManager.getTotalEntitiesInfo();

        ResRoomInfo roomInfo = RequestApiManager.getInstance().getRoomInfoByRoomsApi(totalEntitiesInfo.team.id, totalEntitiesInfo.team.t_defaultChannelId);

        assertThat(roomInfo, is(notNullValue()));
    }
}