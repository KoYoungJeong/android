package com.tosslab.jandi.app.local.orm.repositories;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResChat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by Steve SeongUg Jung on 15. 7. 22..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class ChatRepositoryTest {

    private ChatRepository repository;
    private List<ResChat> originChats;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(RuntimeEnvironment.application);

        repository = ChatRepository.getRepository();

        int memberId = AccountRepository.getRepository().getSelectedTeamInfo().getMemberId();
        originChats = RequestApiManager.getInstance().getChatListByChatApi(memberId);

    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();

    }

    @Test
    public void testUpsertChats() throws Exception {
        boolean success = repository.upsertChats(originChats);

        assertTrue(success);
    }

    @Test
    public void testGetChats() throws Exception {
        repository.upsertChats(originChats);
        List<ResChat> savedChats = repository.getChats();

        assertThat(originChats.size(), is(equalTo(savedChats.size())));
    }

    @Test
    public void testDeleteChat() throws Exception {
        repository.upsertChats(originChats);
        repository.deleteChat(originChats.get(0).getEntityId());
        List<ResChat> savedChatList = repository.getChats();
        assertThat(originChats.size() - 1, is(equalTo(savedChatList.size())));
    }
}