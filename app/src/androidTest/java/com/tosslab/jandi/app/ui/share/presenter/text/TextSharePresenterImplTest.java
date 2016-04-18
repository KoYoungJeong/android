package com.tosslab.jandi.app.ui.share.presenter.text;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.ui.share.views.model.ShareSelectModel_;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class TextSharePresenterImplTest {

    private TextSharePresenterImpl textSharePresenter;
    private TextSharePresenter.View mockView;

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
        textSharePresenter = TextSharePresenterImpl_.getInstance_(JandiApplication.getContext());
        mockView = mock(TextSharePresenter.View.class);
        textSharePresenter.setView(mockView);
        textSharePresenter.shareSelectModel = ShareSelectModel_.getInstance_(JandiApplication.getContext());
        textSharePresenter.shareSelectModel.initFormattedEntities(LeftSideMenuRepository.getRepository().getCurrentLeftSideMenu());
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
        textSharePresenter.teamId = entityManager.getTeamId();

        textSharePresenter.setEntity(entity.getId());

        verify(mockView).setRoomName(eq(entity.getName()));
        verify(mockView).setTeamName(eq(entityManager.getTeamName()));
        verify(mockView).setMentionInfo(eq(entityManager.getTeamId()),
                eq(entity.getId()),
                eq(JandiConstants.TYPE_DIRECT_MESSAGE));
    }

    @Test
    public void testSendMessage() throws Exception {

        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).dismissProgressBar();

        textSharePresenter.teamId = EntityManager.getInstance().getTeamId();
        textSharePresenter.roomId = EntityManager.getInstance().getDefaultTopicId();
        textSharePresenter.sendMessage("hello", new ArrayList<>());

        await().until(() -> finish[0]);

        verify(mockView).showProgressBar();
        verify(mockView).showSuccessToast(anyString());
        verify(mockView).finishOnUiThread();
        verify(mockView).moveEntity(eq(textSharePresenter.teamId),
                eq(textSharePresenter.roomId),
                eq(JandiConstants.TYPE_PUBLIC_TOPIC));
    }
}