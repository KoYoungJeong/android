package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.network.models.start.RawInitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Before;
import org.junit.Test;

import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;


@org.junit.runner.RunWith(AndroidJUnit4.class)
public class RoomMarkerRepositoryTest {

    private static String initializeInfo;
    private static long teamId;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
        teamId = TeamInfoLoader.getInstance().getTeamId();
        initializeInfo = new StartApi(RetrofitBuilder.getInstance()).getRawInitializeInfo(teamId);
    }

    @Before
    public void setUp() throws Exception {
        InitialInfoRepository.getInstance().upsertRawInitialInfo(new RawInitialInfo(teamId, initializeInfo));
        TeamInfoLoader.getInstance().refresh();

    }

    @Test
    public void testUpsertRoomMarkerAndGet() throws Exception {
        long defaultTopicId = TeamInfoLoader.getInstance().getDefaultTopicId();
        long memberId = 1;
        long lastLinkId = 1;
        RoomMarkerRepository.getInstance().upsertRoomMarker(defaultTopicId, memberId, lastLinkId);

        Marker marker = RoomMarkerRepository.getInstance().getMarker(defaultTopicId, memberId);
        assertThat(marker.getMemberId()).isEqualTo(memberId);
        assertThat(marker.getReadLinkId()).isEqualTo(lastLinkId);
    }


    @Test
    public void testGetRoomMarkerCountAndDeleteMarkers() throws Exception {
        long defaultTopicId = TeamInfoLoader.getInstance().getDefaultTopicId();
        long oldRoomMarkerCount = RoomMarkerRepository.getInstance().getRoomMarkerCount(defaultTopicId, Integer.MAX_VALUE);
        assertThat(oldRoomMarkerCount).isLessThanOrEqualTo(TeamInfoLoader.getInstance().getTopicMemberCount(defaultTopicId));

        RoomMarkerRepository.getInstance().deleteMarkers(defaultTopicId);
        assertThat(RoomMarkerRepository.getInstance().getRoomMarkerCount(defaultTopicId, Integer.MAX_VALUE)).isEqualTo(0L);

    }

    @Test
    public void testDeleteMarker() throws Exception {
        long defaultTopicId = TeamInfoLoader.getInstance().getDefaultTopicId();
        long myId = TeamInfoLoader.getInstance().getMyId();
        assertThat(RoomMarkerRepository.getInstance().getMarker(defaultTopicId, myId)).isNotNull();
        assertThat(RoomMarkerRepository.getInstance().deleteMarker(defaultTopicId, myId)).isTrue();
        assertThat(RoomMarkerRepository.getInstance().getMarker(defaultTopicId, myId)).isNull();

    }

}