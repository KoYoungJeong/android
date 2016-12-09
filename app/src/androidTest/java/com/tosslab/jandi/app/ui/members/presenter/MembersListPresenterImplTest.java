package com.tosslab.jandi.app.ui.members.presenter;

import android.support.test.runner.AndroidJUnit4;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.members.dagger.MemberListModule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Component;
import setup.BaseInitUtil;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MembersListPresenterImplTest {

    @Inject
    MembersListPresenterImpl presenter;
    private MembersListPresenterImpl.View mockView;
    private long topicId;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.createDummyTopic();
        BaseInitUtil.inviteDummyMembers();

        topicId = BaseInitUtil.tempTopicId;

        mockView = Mockito.mock(MembersListPresenter.View.class);
        DaggerMembersListPresenterImplTest_TestComponent.builder()
                .memberListModule(new MemberListModule(mockView))
                .build()
                .inject(this);
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.deleteDummyTopic();
    }

    @Test
    public void testOnKickUser() throws Exception {
        long entityId = BaseInitUtil.getUserIdByEmail(BaseInitUtil.TEST2_EMAIL);
        {
            final boolean[] finish = {false};
            Mockito.doAnswer(invocationOnMock -> {
                finish[0] = true;
                return invocationOnMock;
            }).when(mockView).dismissProgressWheel();

            presenter.onKickUser(topicId, entityId);

            Awaitility.await().until(() -> finish[0]);

            Mockito.verify(mockView).showProgressWheel();
            Mockito.verify(mockView).showKickSuccessToast();
            Mockito.verify(mockView).removeUser(Mockito.eq(entityId));
        }

        {
            final boolean[] finish = {false};
            Mockito.doAnswer(invocationOnMock -> {
                finish[0] = true;
                return invocationOnMock;
            }).when(mockView).dismissProgressWheel();

            presenter.onKickUser(topicId, -1);
            Awaitility.await().until(() -> finish[0]);

            Mockito.verify(mockView).refreshMemberList();
            Mockito.verify(mockView).showKickFailToast();
        }

        {
            Mockito.reset(mockView);
            BaseInitUtil.disconnectWifi();

            final boolean[] finish = {false};
            Mockito.doAnswer(invocationOnMock -> {
                finish[0] = true;
                return invocationOnMock;
            }).when(mockView).showKickFailToast();

            presenter.onKickUser(topicId, entityId);
            Awaitility.await().until(() -> finish[0]);

            BaseInitUtil.restoreContext();
            Mockito.verify(mockView).showKickFailToast();
            BaseInitUtil.restoreContext();
        }
    }

    @Test
    public void testGetFilteredChatChooseItems() throws Exception {

        List<ChatChooseItem> members = new ArrayList<>();

        ChatChooseItem chatChooseItem1 = new ChatChooseItem();
        ChatChooseItem chatChooseItem2 = new ChatChooseItem();
        ChatChooseItem chatChooseItem3 = new ChatChooseItem();

        chatChooseItem1.name("abc");
        chatChooseItem2.name("bcd");
        chatChooseItem3.name("cde");

        members.add(chatChooseItem1);
        members.add(chatChooseItem2);
        members.add(chatChooseItem3);

        List<ChatChooseItem> resultList = presenter.getFilteredChatChooseItems("bc", members);
        assertEquals(resultList.size(), 2);
        assertEquals(resultList.get(0), chatChooseItem1);

    }

    @Component(modules = {ApiClientModule.class, MemberListModule.class})
    public interface TestComponent {
        void inject(MembersListPresenterImplTest test);
    }

}