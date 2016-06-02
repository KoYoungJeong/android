package com.tosslab.jandi.app.ui.entities.chats.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Before
    public void setUp() throws Exception {
        chatChooseModel = new ChatChooseModel();
    }


    @Test
    public void testGetChatListWithoutMe() throws Exception {
        List<ChatChooseItem> chatListWithoutMe = chatChooseModel.getChatListWithoutMe(TeamInfoLoader.getInstance().getUserList().get(0).getName().substring(0, 1));
        assertThat(chatListWithoutMe.size(), is(greaterThanOrEqualTo(0)));
    }

    @Test
    public void testGetTeamId() throws Exception {
        long teamId = chatChooseModel.getTeamId();
        assertThat(AccountRepository.getRepository().getSelectedTeamId(), is(equalTo(teamId)));
    }

    @Test
    public void testGetUsers() throws Exception {
        List<ChatChooseItem> users = chatChooseModel.getUsers();
        assertThat(users.size(), is(lessThanOrEqualTo(TeamInfoLoader.getInstance().getUserList().size() + 1)));
    }
}