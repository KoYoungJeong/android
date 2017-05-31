package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.presenter;

import android.support.test.runner.AndroidJUnit4;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.network.client.publictopic.ChannelApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.dagger.TopicCreateModule;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.Date;

import javax.inject.Inject;

import dagger.Component;
import setup.BaseInitUtil;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
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

    @Inject
    TopicCreatePresenter topicCreatePresenter;
    private TopicCreatePresenter.View mockView;

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
        mockView = Mockito.mock(TopicCreatePresenter.View.class);

        DaggerTopicCreatePresenterImplTest_TestComponent.builder()
                .topicCreateModule(new TopicCreateModule(mockView))
                .build()
                .inject(this);

    }

    @Component(modules = {ApiClientModule.class, TopicCreateModule.class})
    interface TestComponent {
        void inject(TopicCreatePresenterImplTest test);
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
        }).when(mockView).createTopicSuccess(anyLong(), anyLong(), anyString(), anyBoolean());

        // when
        String topicName = "aaaaa123zca" + new Date().toString();
        topicCreatePresenter.onCreateTopic(topicName, "", true, false);

        Awaitility.await().until(() -> finish[0]);

        // then
        verify(mockView, atLeast(1)).dismissProgressWheel();
        verify(mockView, times(1)).showProgressWheel();
        if (teamId[0] > 0) {
            verify(mockView, times(1)).createTopicSuccess(eq(teamId[0]), eq(topicId[0]), eq(topicName), eq(true));
            // restore
            new ChannelApi(InnerApiRetrofitBuilder.getInstance()).deleteTopic(topicId[0], new ReqDeleteTopic(teamId[0]));
        }

    }
}