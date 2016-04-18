package com.tosslab.jandi.app.ui.entities.chats.presenter;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.entities.chats.adapter.ChatChooseAdapter;
import com.tosslab.jandi.app.ui.entities.chats.adapter.ChatChooseAdapterDataModel;
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
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class ChatChoosePresenterImplTest {

    private ChatChoosePresenterImpl presenter;
    private ChatChoosePresenter.View mockView;
    private ChatChooseAdapterDataModel dataModel;

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

        dataModel = spy(new ChatChooseAdapter(JandiApplication.getContext()));
        presenter = new ChatChoosePresenterImpl(new ChatChooseModel(), TeamDomainInfoModel_.getInstance_(JandiApplication.getContext()), InvitationDialogExecutor_.getInstance_(JandiApplication.getContext()), mockView, dataModel);
    }


    @Test
    public void testInitMembers() throws Exception {
        boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).refresh();
        presenter.initMembers();
        await().until(() -> finish[0]);
        verify(mockView).refresh();
    }

    @Test
    public void testOnSearch() throws Exception {
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> finish[0] = true)
                .when(mockView).refresh();

        presenter.onSearch("a");

        await().until(() -> finish[0]);

        verify(dataModel).clear();
        verify(mockView).refresh();
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
        final boolean[] finish = {false};
        doAnswer(mock -> {
            finish[0] = true;
            return mock;
        }).when(mockView).moveChatMessage(anyLong(), anyLong());
        presenter.onMoveChatMessage(1L);
        await().until(() -> finish[0]);
        verify(mockView).moveChatMessage(eq(EntityManager.getInstance().getTeamId()), eq(1L));
    }
}