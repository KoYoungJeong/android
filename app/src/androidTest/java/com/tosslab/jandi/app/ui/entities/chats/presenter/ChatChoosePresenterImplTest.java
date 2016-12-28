package com.tosslab.jandi.app.ui.entities.chats.presenter;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.entities.chats.adapter.ChatChooseAdapter;
import com.tosslab.jandi.app.ui.entities.chats.adapter.ChatChooseAdapterDataModel;
import com.tosslab.jandi.app.ui.entities.chats.dagger.ChatChooseModule;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import dagger.Component;
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

    @Inject
    ChatChoosePresenter presenter;
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
        DaggerChatChoosePresenterImplTest_TestComponent.builder()
                .chatChooseModule(new ChatChooseModule(mockView, ((ChatChooseAdapter) dataModel)))
                .build()
                .inject(this);
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
        verify(mockView).moveChatMessage(eq(TeamInfoLoader.getInstance().getTeamId()), eq(1L));
    }

    @Component(modules = {ApiClientModule.class, ChatChooseModule.class})
    public interface TestComponent {
        void inject(ChatChoosePresenterImplTest test);
    }
}