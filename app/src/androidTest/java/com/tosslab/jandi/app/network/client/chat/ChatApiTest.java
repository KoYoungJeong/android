package com.tosslab.jandi.app.network.client.chat;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.start.RawInitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
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
        long targetUserId = Observable.from(rooms)
                .takeFirst(DirectMessageRoom::isJoined)
                .map(DirectMessageRoom::getCompanionId)
                .toBlocking().firstOrDefault(-1L);
        if (targetUserId > 0) {
            ResCommon resCommon = chatApi.deleteChat(TeamInfoLoader.getInstance().getTeamId(), targetUserId);
            assertThat(resCommon).isNotNull();
        }
    }

    @Test
    public void createChat() throws Exception {
        User jandiBot = TeamInfoLoader.getInstance().getJandiBot();

        long teamId = TeamInfoLoader.getInstance().getTeamId();
        ResCommon chat = chatApi.createChat(teamId, jandiBot.getId());

        long chatId = TeamInfoLoader.getInstance().getChatId(jandiBot.getId());
        if (chatId > 0) {
            assertThat(chat.id).isEqualTo(chatId);
        } else {
            String rawInitializeInfo = new StartApi(RetrofitBuilder.getInstance()).getRawInitializeInfo(teamId);
            InitialInfoRepository.getInstance().upsertRawInitialInfo(new RawInitialInfo(teamId, rawInitializeInfo));
            TeamInfoLoader.getInstance().refresh();
            assertThat(TeamInfoLoader.getInstance().isChat(chat.id)).isTrue();

            assertThat(TeamInfoLoader.getInstance().isChat(jandiBot.getId())).isTrue();
        }
    }
}