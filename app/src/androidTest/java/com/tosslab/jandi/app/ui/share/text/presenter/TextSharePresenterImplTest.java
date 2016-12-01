package com.tosslab.jandi.app.ui.share.text.presenter;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.share.text.dagger.TextShareModule;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.Component;
import rx.Observable;
import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class TextSharePresenterImplTest {

    @Inject
    TextSharePresenter textSharePresenter;
    private TextSharePresenter.View mockView;

    @Component(modules = {TextShareModule.class, ApiClientModule.class})
    public interface TestComponent {
        void inject(TextSharePresenterImplTest test);
    }

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
        mockView = mock(TextSharePresenter.View.class);
        DaggerTextSharePresenterImplTest_TestComponent.builder()
                .textShareModule(new TextShareModule(mockView))
                .build()
                .inject(this);

        ((TextSharePresenterImpl) textSharePresenter).teamInfoLoader = TeamInfoLoader.getInstance();
    }

    @Test
    public void testInitEntityData() throws Exception {
        {
            final boolean[] finish = {false};
            doAnswer(invocationOnMock -> {
                finish[0] = true;
                return invocationOnMock;
            }).when(mockView).setMentionInfo(anyInt(), anyInt(), anyInt());
            textSharePresenter.initViews();

            await().until(() -> finish[0]);

            verify(mockView).setRoomName(eq(TeamInfoLoader.getInstance().getName(TeamInfoLoader.getInstance().getDefaultTopicId())));
            verify(mockView).setTeamName(eq(TeamInfoLoader.getInstance().getTeamName()));
            verify(mockView).setMentionInfo(eq(TeamInfoLoader.getInstance().getTeamId()),
                    eq(TeamInfoLoader.getInstance().getDefaultTopicId()),
                    eq(JandiConstants.TYPE_PUBLIC_TOPIC));
        }
    }

    @Test
    public void testSetEntity() throws Exception {
        User entity = Observable.from(TeamInfoLoader.getInstance().getUserList())
                .takeFirst(user -> user.getId() != TeamInfoLoader.getInstance().getMyId())
                .toBlocking().first();
        ((TextSharePresenterImpl) textSharePresenter).teamId = TeamInfoLoader.getInstance().getTeamId();

        textSharePresenter.setEntity(entity.getId(), JandiConstants.TYPE_DIRECT_MESSAGE);

        verify(mockView).setRoomName(eq(entity.getName()));
        verify(mockView).setTeamName(eq(TeamInfoLoader.getInstance().getTeamName()));
        verify(mockView).setMentionInfo(eq(TeamInfoLoader.getInstance().getTeamId()),
                eq(entity.getId()),
                eq(JandiConstants.TYPE_DIRECT_MESSAGE));
    }

    @Test
    public void testSendMessage() throws Exception {

        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).moveEntity(anyLong(), anyLong(), anyLong(), anyInt());

        TextSharePresenterImpl impl = (TextSharePresenterImpl) this.textSharePresenter;

        impl.teamId = TeamInfoLoader.getInstance().getTeamId();
        impl.roomId = TeamInfoLoader.getInstance().getDefaultTopicId();
        impl.sendMessage("hello", new ArrayList<>());

        await().until(() -> finish[0]);

        verify(mockView).showProgressBar();
        verify(mockView).showSuccessToast(anyString());
        verify(mockView).finishOnUiThread();
        verify(mockView).moveEntity(eq(impl.teamId),
                eq(impl.roomId),
                eq(imple.entityId), anyInt());
    }
}