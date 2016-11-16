package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.start.LastMessage;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Before;
import org.junit.Test;

import rx.Observable;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;


@org.junit.runner.RunWith(AndroidJUnit4.class)
public class ChatRepositoryTest {
    private static InitialInfo initializeInfo;
    private static Observable<Chat> chatObservable;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
        initializeInfo = new StartApi(RetrofitBuilder.getInstance()).getInitializeInfo(TeamInfoLoader.getInstance().getTeamId());

        chatObservable = Observable.from(initializeInfo.getChats())
                .takeFirst(chat -> true)
                .replay()
                .refCount();


    }

    @Before
    public void setUp() throws Exception {
        InitialInfoRepository.getInstance().upsertInitialInfo(initializeInfo);
        TeamInfoLoader.getInstance().refresh();

    }

    private Chat getChat() {
        return chatObservable.toBlocking().first();
    }

    @Test
    public void testIncrementUnreadCount() throws Exception {
        Chat chat = getChat();

        ChatRepository.getInstance().incrementUnreadCount(chat.getId());

        Chat chat1 = ChatRepository.getInstance().getChat(chat.getId());

        assertThat(chat1.getUnreadCount()).isGreaterThan(chat.getUnreadCount());
        assertThat(chat1.getUnreadCount()).isEqualTo(chat.getUnreadCount() + 1);
    }

    @Test
    public void testUpdateChatOpened() throws Exception {
        Chat chat = getChat();
        ChatRepository.getInstance().updateChatOpened(chat.getId(), !chat.isOpened());

        Chat chat1 = ChatRepository.getInstance().getChat(chat.getId());
        assertThat(chat1.isOpened()).isEqualTo(!chat.isOpened());
    }

    @Test
    public void testGetChat() throws Exception {
        Chat chat = getChat();
        Chat chat1 = ChatRepository.getInstance().getChat(chat.getId());

        assertThat(chat1).isNotNull();
    }

    @Test
    public void testAddChat() throws Exception {
        Chat chat = getChat();
        chat.setId(1);
        ChatRepository.getInstance().addChat(chat);

        Chat chat1 = ChatRepository.getInstance().getChat(1);

        assertThat(chat1.getId()).isEqualTo(chat.getId());
        assertThat(chat1.getUnreadCount()).isEqualTo(chat.getUnreadCount());
        assertThat(chat1.getLastLinkId()).isEqualTo(chat.getLastLinkId());
        assertThat(chat1.getReadLinkId()).isEqualTo(chat.getReadLinkId());
        assertThat(chat1.getType()).isEqualTo(chat.getType());

        ChatRepository.getInstance().deleteChat(1);
    }

    @Test
    public void testUpdateLastMessage() throws Exception {
        Chat chat = getChat();
        String text = "hello";
        String status = "created";
        ChatRepository.getInstance().updateLastMessage(chat.getId(), 1, text, status);

        LastMessage lastMessage = ChatRepository.getInstance().getChat(chat.getId()).getLastMessage();

        assertThat(lastMessage.getId()).isEqualTo(1l);
        assertThat(lastMessage.getStatus()).isEqualToIgnoringCase(status);
        assertThat(lastMessage.getText()).isEqualToIgnoringCase(text);


    }

    @Test
    public void testIsChat() throws Exception {
        assertThat(ChatRepository.getInstance().isChat(-1l)).isFalse();
        assertThat(ChatRepository.getInstance().isChat(getChat().getId())).isTrue();

    }

    @Test
    public void testUpdateUnreadCount() throws Exception {
        Chat chat = getChat();
        ChatRepository.getInstance().updateUnreadCount(chat.getId(), chat.getUnreadCount() + 1);

        int unreadCount = ChatRepository.getInstance().getChat(chat.getId()).getUnreadCount();

        assertThat(unreadCount).isEqualTo(chat.getUnreadCount() + 1);
    }

    @Test
    public void testUpdateLastLinkId() throws Exception {
        Chat chat = getChat();
        ChatRepository.getInstance().updateLastLinkId(chat.getId(), chat.getLastLinkId() + 1);

        long lastLinkId = ChatRepository.getInstance().getChat(chat.getId()).getLastLinkId();

        assertThat(lastLinkId).isEqualTo(chat.getLastLinkId() + 1);

    }

    @Test
    public void testUpdateReadLinkId() throws Exception {
        Chat chat = getChat();
        ChatRepository.getInstance().updateReadLinkId(chat.getId(), chat.getReadLinkId() + 1);

        long readLinkId = ChatRepository.getInstance().getChat(chat.getId()).getReadLinkId();
        assertThat(readLinkId).isEqualTo(chat.getReadLinkId() + 1);
    }
}