package com.tosslab.jandi.app.ui.maintab.topic.views.create.presenter;

import android.support.test.runner.AndroidJUnit4;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.Date;

import setup.BaseInitUtil;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by jsuch2362 on 15. 11. 19..
 */
@RunWith(AndroidJUnit4.class)
public class TopicCreatePresenterImplTest {

    private TopicCreatePresenter topicCreatePresenter;
    private TopicCreatePresenter.View mockView;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();
        topicCreatePresenter = TopicCreatePresenterImpl_.getInstance_(JandiApplication.getContext());
        mockView = Mockito.mock(TopicCreatePresenter.View.class);

        topicCreatePresenter.setView(mockView);
    }

    @Test
    public void testCreateTopic_Network_Off() throws Exception {
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).showCheckNetworkDialog();
        BaseInitUtil.disconnectWifi();
        topicCreatePresenter.onCreateTopic("a", "", true, false);

        Awaitility.await().until(() -> finish[0]);

        verify(mockView, times(1)).showCheckNetworkDialog();
        BaseInitUtil.restoreContext();
    }

    @Test
    public void testCreateTopic() throws Exception {
        Mockito.reset(mockView);
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).dismissProgressWheel();

        final long[] teamId = {-1};
        final long[] topicId = {-1};
        doAnswer(invocationOnMock -> {
            topicId[0] = (Long) invocationOnMock.getArguments()[1];
            teamId[0] = (Long) invocationOnMock.getArguments()[0];
            return invocationOnMock;
        }).when(mockView).createTopicSuccess(anyInt(), anyInt(), anyString(), anyBoolean());

        // when
        String topicName = "aaaaa123zca" + new Date().toString();
        topicCreatePresenter.onCreateTopic(topicName, "", true, false);

        Awaitility.await().until(() -> finish[0]);

        // then
        verify(mockView, atLeast(1)).dismissProgressWheel();
        verify(mockView, times(1)).showProgressWheel();
        if (teamId[0] > 0) {
            verify(mockView, times(1)).createTopicSuccess(eq(teamId[0]), eq(topicId[0]), eq(topicName), eq(true));
        }

        // restore
        RequestApiManager.getInstance().deleteTopicByChannelApi(topicId[0], new ReqDeleteTopic(teamId[0]));
    }
}