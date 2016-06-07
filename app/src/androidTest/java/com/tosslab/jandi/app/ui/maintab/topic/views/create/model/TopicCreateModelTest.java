package com.tosslab.jandi.app.ui.maintab.topic.views.create.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.client.privatetopic.GroupApi;
import com.tosslab.jandi.app.network.client.publictopic.ChannelApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import setup.BaseInitUtil;

import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(AndroidJUnit4.class)
public class TopicCreateModelTest {

    private static final String TAG = TopicCreateModelTest.class.getName();
    private TopicCreateModel topicCreateModel;

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

        topicCreateModel = TopicCreateModel_.getInstance_(JandiApplication.getContext());
    }

    @Test
    public void testCreateTopic() throws Exception {
        {
            // When
            String topicName = "haha";
            String topicDescription = "haha2";
            ResCommon topic = topicCreateModel.createTopic(topicName, true, topicDescription, true);

            // Then
            TopicRoom topicRoom = TeamInfoLoader.getInstance().getTopic(topic.id);
            assertThat(topicRoom, is(notNullValue()));
            assertThat(topicRoom.getName(), is(equalTo(topicName)));
            assertThat(topicRoom.getDescription(), is(equalTo(topicDescription)));
            assertThat(topicRoom.isAutoJoin(), is(true));

            // Restore
            new ChannelApi(RetrofitBuilder.getInstance())
                    .deleteTopic(topic.id, new ReqDeleteTopic(TeamInfoLoader.getInstance().getTeamId()));
        }

        {
            // When
            String topicName = "haha";
            String topicDescription = "haha2";
            ResCommon topic = null;
            try {
                topic = topicCreateModel.createTopic(topicName, false, topicDescription, true);
                fail("절대로 성공하면 안됨");
            } catch (RetrofitException retrofitError) {
                retrofitError.printStackTrace();
            }
        }

        {
            // When
            String topicName = "haha" + new Date().toString();
            String topicDescription = "haha2" + new Date().toString();
            ResCommon topic = topicCreateModel.createTopic(topicName, false, topicDescription, false);
            Thread.sleep(200);

            // Then
            TopicRoom entity = TeamInfoLoader.getInstance().getTopic(topic.id);
            assertThat(entity, is(notNullValue()));
            assertThat(entity.getName(), is(equalTo(topicName)));
            assertThat(entity.getDescription(), is(equalTo(topicDescription)));
            assertThat(entity.isAutoJoin(), is(false));

            // Restore
            new GroupApi(RetrofitBuilder.getInstance())
                    .deleteGroup(TeamInfoLoader.getInstance().getTeamId(), topic.id);
        }

    }

    @Test
    public void testInvalideTitle() throws Exception {
        {
            boolean valid = topicCreateModel.invalideTitle("");
            assertThat(valid, is(true));
        }
        {
            boolean valid = topicCreateModel.invalideTitle("aa");
            assertThat(valid, is(false));
        }
    }
}