package com.tosslab.jandi.app.ui.share.presenter.text;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class TextSharePresenterImplTest {

    private TextSharePresenter textSharePresenter;
    private TextSharePresenter.View mockView;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();
        textSharePresenter = TextSharePresenterImpl_.getInstance_(JandiApplication.getContext());
        mockView = mock(TextSharePresenter.View.class);
        textSharePresenter.setView(mockView);
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.clear();
    }

    @Test
    public void testInitViews() throws Exception {
        {
            BaseInitUtil.disconnectWifi();

            textSharePresenter.initViews();

            verify(mockView, times(1)).showFailToast(anyString());
            verify(mockView, times(1)).finishOnUiThread();

            BaseInitUtil.restoreContext();
        }

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

            EntityManager entityManager = EntityManager.getInstance();
            verify(mockView).setRoomName(eq(entityManager.getEntityNameById(entityManager.getDefaultTopicId())));
            verify(mockView).setTeamName(eq(entityManager.getTeamName()));
            verify(mockView).setMentionInfo(eq(entityManager.getTeamId()),
                    eq(entityManager.getDefaultTopicId()),
                    eq(JandiConstants.TYPE_PUBLIC_TOPIC));
        }
    }

    @Test
    public void testSetEntity() throws Exception {
        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entity = entityManager.getFormattedUsersWithoutMe().get(0);
        textSharePresenter.setEntity(entity.getId());
        verify(mockView).setRoomName(eq(entityManager.getEntityNameById(entityManager.getDefaultTopicId())));
        verify(mockView).setTeamName(eq(entityManager.getTeamName()));
        verify(mockView).setMentionInfo(eq(entityManager.getTeamId()),
                eq(entityManager.getDefaultTopicId()),
                eq(JandiConstants.TYPE_PUBLIC_TOPIC));
    }

    @Test
    public void testSetView() throws Exception {

    }

    @Test
    public void testGetTeamId() throws Exception {

    }

    @Test
    public void testSendMessage() throws Exception {

    }
}