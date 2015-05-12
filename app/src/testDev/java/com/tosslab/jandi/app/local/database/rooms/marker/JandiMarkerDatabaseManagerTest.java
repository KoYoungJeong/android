package com.tosslab.jandi.app.local.database.rooms.marker;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.network.models.ResRoomInfo;
import com.tosslab.jandi.app.ui.maintab.chat.model.ChatListRequest;
import com.tosslab.jandi.app.ui.message.v2.model.RoomMarkerRequest;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.List;
import java.util.concurrent.Callable;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Steve SeongUg Jung on 15. 5. 12..
 */
@RunWith(RobolectricGradleTestRunner.class)
public class JandiMarkerDatabaseManagerTest {

    private JandiRestClient jandiRestClient;
    private JandiMarkerDatabaseManager databaseManager;

    @Before
    public void setUp() throws Exception {

        BaseInitUtil.initData(Robolectric.application);
        jandiRestClient = new JandiRestClient_(Robolectric.application);
        jandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(Robolectric.application));

        databaseManager = JandiMarkerDatabaseManager.getInstance(Robolectric.application);
    }

    @Test
    public void testMultiThreadUpsert() throws Exception {

        ResAccountInfo.UserTeam userTeam = JandiAccountDatabaseManager.getInstance(Robolectric.application).getUserTeams().get(0);
        int teamId = userTeam.getTeamId();
        List<ResChat> chatList = RequestManager.newInstance(Robolectric.application, ChatListRequest.create(Robolectric.application, userTeam.getMemberId())).request();

        ResRoomInfo roomInfo = RequestManager.newInstance(Robolectric.application, RoomMarkerRequest.create(Robolectric.application, teamId, chatList.get(0).getEntityId())).request();

        final boolean[] finish = new boolean[2];

        new Thread(new Runnable() {
            @Override
            public void run() {
                databaseManager.upsertMarkers(roomInfo);
                System.out.println("Complete to Upsert");
                finish[0] = true;
            }
        }).start();

        for (ResRoomInfo.MarkerInfo markerInfo : roomInfo.getMarkers()) {
            databaseManager.updateMarker(teamId, chatList.get(0).getEntityId(), markerInfo.getMemberId(), markerInfo.getLastLinkId());
        }

        finish[1] = true;
        System.out.println("Complete to Update");

        Awaitility.await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return finish[0] & finish[1];
            }
        });

        List<ResRoomInfo.MarkerInfo> markers = databaseManager.getMarkers(teamId, chatList.get(0).getEntityId());

        assertThat(markers.size(), is(2));
    }
}