package com.tosslab.jandi.app.local.database.rooms.marker;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MarkerRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.network.models.ResRoomInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Steve SeongUg Jung on 15. 5. 12..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class JandiMarkerDatabaseManagerTest {

    @Before
    public void setUp() throws Exception {

        BaseInitUtil.initData(RuntimeEnvironment.application);

    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();

    }

    @Test
    public void testMultiThreadUpsert() throws Exception {

        ResAccountInfo.UserTeam userTeam = AccountRepository.getRepository().getAccountTeams().get(0);
        int teamId = userTeam.getTeamId();
        List<ResChat> chatList = RequestApiManager.getInstance().getChatListByChatApi(teamId);
        ResRoomInfo roomInfo = RequestApiManager.getInstance().getRoomInfoByRoomsApi(teamId, chatList.get(0).getEntityId());
        final boolean[] finish = new boolean[2];

        new Thread(new Runnable() {
            @Override
            public void run() {
                MarkerRepository.getRepository().upsertRoomInfo(roomInfo);
                System.out.println("Complete to Upsert");
                finish[0] = true;
            }
        }).start();

        for (ResRoomInfo.MarkerInfo markerInfo : roomInfo.getMarkers()) {
            MarkerRepository.getRepository().upsertRoomMarker(teamId, chatList.get(0).getEntityId(), markerInfo.getMemberId(), markerInfo.getLastLinkId());
        }

        finish[1] = true;
        System.out.println("Complete to Update");

        Awaitility.await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return finish[0] & finish[1];
            }
        });

        Collection<ResRoomInfo.MarkerInfo> markers = MarkerRepository.getRepository().getRoomMarker(teamId, chatList.get(0).getEntityId());

        assertThat(markers.size(), is(2));
    }

    @Test
    public void testUpdateMarker() throws Exception {

        int exampleKey = 1;
        int currentMarker = 100;
        int oldMarker = 99;
        int newMarker = 101;

        // 현재 마커 추가
        boolean count = MarkerRepository.getRepository().upsertRoomMarker(exampleKey, exampleKey, exampleKey,
                currentMarker);

        assertThat(count, is(true));

        Collection<ResRoomInfo.MarkerInfo> markers = MarkerRepository.getRepository().getRoomMarker(exampleKey,
                exampleKey);

        // 현재 마커 확인
        assertThat(markers.size(), is(1));
        assertThat(markers.iterator().next().getLastLinkId(), is(currentMarker));

        // 예전 마커 추가
        count = MarkerRepository.getRepository().upsertRoomMarker(exampleKey, exampleKey, exampleKey, oldMarker);
        assertThat(count, is(true));

        markers = MarkerRepository.getRepository().getRoomMarker(exampleKey, exampleKey);

        // 예전 마커 갱신 안됐는지 확인
        assertThat(markers.size(), is(1));
        assertThat(markers.iterator().next().getLastLinkId(), is(currentMarker));


        // 새로운 마커 추가
        count = MarkerRepository.getRepository().upsertRoomMarker(exampleKey, exampleKey, exampleKey, newMarker);

        assertThat(count, is(true));

        markers = MarkerRepository.getRepository().getRoomMarker(exampleKey, exampleKey);

        // 새로운 마커 확인
        assertThat(markers.size(), is(1));
        assertThat(markers.iterator().next().getLastLinkId(), is(newMarker));


    }
}