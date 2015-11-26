package com.tosslab.jandi.app.ui.members.presenter;

import android.support.test.runner.AndroidJUnit4;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.members.MembersListActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import setup.BaseInitUtil;

@RunWith(AndroidJUnit4.class)
public class MembersListPresenterImplTest {

    private MembersListPresenterImpl presenter;
    private MembersListPresenterImpl.View mockView;
    private int topicId;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.createDummyTopic();
        BaseInitUtil.inviteDummyMembers();

        topicId = BaseInitUtil.tempTopicId;

        presenter = MembersListPresenterImpl_.getInstance_(JandiApplication.getContext());
        mockView = Mockito.mock(MembersListPresenter.View.class);
        presenter.setView(mockView);

    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.deleteDummyTopic();
    }

    @Test
    public void testOnKickUser() throws Exception {
        int entityId = BaseInitUtil.getUserIdByEmail(BaseInitUtil.TEST2_EMAIL);
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
            BaseInitUtil.disconnectWifi();

            final boolean[] finish = {false};
            Mockito.doAnswer(invocationOnMock -> {
                finish[0] = true;
                return invocationOnMock;
            }).when(mockView).showKickFailToast();

            presenter.onKickUser(topicId, entityId);
            Awaitility.await().until(() -> finish[0]);

            Mockito.verify(mockView).showKickFailToast();
            BaseInitUtil.restoreContext();
        }
    }

    @Test
    public void testGetFilteredChatChooseItems() throws Exception {

    }

    @Test
    public void testGetChatChooseItems() throws Exception {

        Mockito.doAnswer(invocationOnMock -> {

        }).doReturn()
        presenter.getChatChooseItems();


        Mockito.when(mockView.getEntityId()).thenReturn(BaseInitUtil.getUserIdByEmail(BaseInitUtil.TEST2_EMAIL));
        Mockito.when(mockView.getType()).thenReturn(MembersListActivity.TYPE_MEMBERS_JOINABLE_TOPIC);
        Mockito.when(mockView.getType()).thenReturn(MembersListActivity.TYPE_MEMBERS_JOINABLE_TOPIC);
        Mockito.when(mockView.getType()).thenReturn(MembersListActivity.TYPE_MEMBERS_JOINABLE_TOPIC);
        Mockito.when(mockView.getType()).thenReturn(MembersListActivity.TYPE_MEMBERS_JOINABLE_TOPIC);
    }
}