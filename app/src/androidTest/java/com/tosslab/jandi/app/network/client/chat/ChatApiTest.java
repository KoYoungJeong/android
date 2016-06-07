package com.tosslab.jandi.app.network.client.chat;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.DirectMessageRoom;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import rx.Observable;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(AndroidJUnit4.class)
public class ChatApiTest {

    private ChatApi chatApi;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {
        chatApi = new ChatApi(RetrofitBuilder.getInstance());
    }

    @Test
    public void testDeleteChat() throws Exception {
        List<DirectMessageRoom> rooms = TeamInfoLoader.getInstance().getDirectMessageRooms();
        if (rooms.size() <= 0) return;
        Long targetUserId = Observable.from(rooms.get(0).getMembers()).takeFirst(memberId -> memberId != TeamInfoLoader.getInstance().getMyId()).toBlocking().first();
        ResCommon resCommon = chatApi.deleteChat(TeamInfoLoader.getInstance().getMyId(), targetUserId);
        assertThat(resCommon).isNotNull();
    }
}