package com.tosslab.jandi.app.local.orm.repositories;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResRoomInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Collection;

import rx.Observable;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Steve SeongUg Jung on 15. 7. 23..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class MarkerRepositoryTest {

    private ResRoomInfo originRoomInfo;
    private int teamId;
    private int roomId;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(RuntimeEnvironment.application);

        teamId = AccountRepository.getRepository().getSelectedTeamId();

        ResLeftSideMenu leftSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(teamId);
        roomId = leftSideMenu.team.t_defaultChannelId;

        originRoomInfo = RequestApiManager.getInstance().getRoomInfoByRoomsApi(teamId, roomId);
    }
    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();

    }

    @Test
    public void testRepository() throws Exception {
        boolean success = MarkerRepository.getRepository().upsertRoomInfo(originRoomInfo);
        assertThat(success, is(true));

        Collection<ResRoomInfo.MarkerInfo> roomMarker = MarkerRepository.getRepository().getRoomMarker(teamId, roomId);

        assertThat(originRoomInfo.getMarkers().size(), is(equalTo(roomMarker.size())));

        ResRoomInfo.MarkerInfo item = originRoomInfo.getMarkers().iterator().next();
        item.setLastLinkId(item.getLastLinkId() + 1);

        MarkerRepository.getRepository().upsertRoomMarker(teamId, roomId, item.getMemberId(),
                item.getLastLinkId());

        roomMarker = MarkerRepository.getRepository().getRoomMarker(teamId, roomId);
        ResRoomInfo.MarkerInfo first = Observable.from(roomMarker)
                .filter(markerInfo -> markerInfo.getMemberId() == item.getMemberId())
                .toBlocking()
                .first();

        assertThat(first.getLastLinkId(), is(equalTo(item.getLastLinkId())));

        MarkerRepository.getRepository().deleteRoomMarker(roomId, item.getMemberId());
        roomMarker = MarkerRepository.getRepository().getRoomMarker(teamId, roomId);

        assertThat(roomMarker.size(), is(equalTo(originRoomInfo.getMarkers().size() - 1)));


    }

}