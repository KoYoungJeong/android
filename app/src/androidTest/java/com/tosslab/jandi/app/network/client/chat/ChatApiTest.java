package com.tosslab.jandi.app.network.client.chat;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.network.models.ResCommon;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

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
    public void testGetChatList() throws Exception {
        List<ResChat> chatList = chatApi.getChatList(EntityManager.getInstance().getMe().getId());
        assertThat(chatList).isNotNull();
        assertThat(chatList.size()).isGreaterThanOrEqualTo(0);
    }

    @Test
    public void testDeleteChat() throws Exception {
        List<ResChat> chatList = chatApi.getChatList(EntityManager.getInstance().getMe().getId());
        if (chatList.size() <= 0) return;
        ResCommon resCommon = chatApi.deleteChat(EntityManager.getInstance().getMe().getId(), chatList.get(0).getCompanionId());
        assertThat(resCommon).isNotNull();
    }
}