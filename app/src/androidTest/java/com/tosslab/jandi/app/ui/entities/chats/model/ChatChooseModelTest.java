package com.tosslab.jandi.app.ui.entities.chats.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import setup.BaseInitUtil;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class ChatChooseModelTest {

    private ChatChooseModel chatChooseModel;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();
        chatChooseModel = ChatChooseModel_.getInstance_(JandiApplication.getContext());
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.clear();
    }

    @Test
    public void testGetChatListWithoutMe() throws Exception {
        List<ChatChooseItem> chatListWithoutMe = chatChooseModel.getChatListWithoutMe(EntityManager.getInstance().getFormattedUsersWithoutMe().get(0).getName().substring(0, 1));
        assertThat(chatListWithoutMe.size(), is(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGetTeamId() throws Exception {
        long teamId = chatChooseModel.getTeamId();
        assertThat(EntityManager.getInstance().getTeamId(), is(equalTo(teamId)));
    }

    @Test
    public void testGetUsers() throws Exception {
        List<ChatChooseItem> users = chatChooseModel.getUsers();
        assertThat(users.size(), is(lessThanOrEqualTo(EntityManager.getInstance().getFormattedUsersWithoutMe().size() + 2)));
    }
}