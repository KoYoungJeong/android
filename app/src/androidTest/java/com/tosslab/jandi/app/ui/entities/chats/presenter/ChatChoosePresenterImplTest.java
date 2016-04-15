package com.tosslab.jandi.app.ui.entities.chats.presenter;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.entities.chats.model.ChatChooseModel;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor_;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel_;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class ChatChoosePresenterImplTest {

    private ChatChoosePresenterImpl presenter;
    private ChatChoosePresenter.View mockView;

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
        mockView = mock(ChatChoosePresenter.View.class);
        presenter = new ChatChoosePresenterImpl(new ChatChooseModel(), TeamDomainInfoModel_.getInstance_(JandiApplication.getContext()), InvitationDialogExecutor_.getInstance_(JandiApplication.getContext()), mockView, chatChooseAdapter);
    }


    @Test
    public void testInitMembers() throws Exception {
        presenter.initMembers();
        verify(mockView).setUsers(anyList());
    }

    @Test
    public void testOnSearch() throws Exception {
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> finish[0] = true)
                .when(mockView).setUsers(anyList());

        presenter.onSearch("a");

        await().until(() -> finish[0]);

        verify(mockView).setUsers(anyList());
    }

    @Test
    public void testInvite() throws Exception {
        presenter.invitationDialogExecutor = mock(InvitationDialogExecutor.class);

        presenter.invite();

        verify(presenter.invitationDialogExecutor).execute();
        verify(presenter.invitationDialogExecutor).setFrom(eq(InvitationDialogExecutor.FROM_CHAT_CHOOSE));
    }

    @Test
    public void testOnMoveChatMessage() throws Exception {
        presenter.onMoveChatMessage(1L);
        verify(mockView).moveChatMessage(eq(EntityManager.getInstance().getTeamId()), eq(1L));
    }
}