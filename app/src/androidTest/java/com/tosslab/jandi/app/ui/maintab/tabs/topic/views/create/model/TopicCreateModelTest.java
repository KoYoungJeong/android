package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.create.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.client.privatetopic.GroupApi;
import com.tosslab.jandi.app.network.client.publictopic.ChannelApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.start.RawInitialInfo;
import com.tosslab.jandi.app.network.models.start.Topic;
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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
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

        topicCreateModel = new TopicCreateModel(() -> new ChannelApi(InnerApiRetrofitBuilder.getInstance()),
                () -> new GroupApi(InnerApiRetrofitBuilder.getInstance()));
    }

    @Test
    public void testCreateTopic() throws Exception {
        {
            // When
            String topicName = "haha_" + new Date().toString();
            String topicDescription = "haha2";
            Topic topic = topicCreateModel.createTopic(topicName, true, topicDescription, true);

            // Then
            assertThat(topic, is(notNullValue()));
            assertThat(topic.getId(), is(greaterThanOrEqualTo(0L)));

            // Restore
            new ChannelApi(InnerApiRetrofitBuilder.getInstance())
                    .deleteTopic(topic.getId(), new ReqDeleteTopic(TeamInfoLoader.getInstance().getTeamId()));
        }

        {
            // When
            String topicName = "haha";
            String topicDescription = "haha2";
            Topic topic = null;
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
            Topic topic = topicCreateModel.createTopic(topicName, false, topicDescription, false);
            long teamId = TeamInfoLoader.getInstance().getTeamId();
            String initializeInfo = new StartApi(InnerApiRetrofitBuilder.getInstance()).getRawInitializeInfo(teamId);
            InitialInfoRepository.getInstance().upsertRawInitialInfo(new RawInitialInfo(teamId, initializeInfo));
            TeamInfoLoader.getInstance().refresh();

            // Then
            TopicRoom entity = TeamInfoLoader.getInstance().getTopic(topic.getId());
            assertThat(entity, is(notNullValue()));
            assertThat(entity.getName(), is(equalTo(topicName)));
            assertThat(entity.getDescription(), is(equalTo(topicDescription)));
            assertThat(entity.isAutoJoin(), is(false));

            // Restore
            new GroupApi(InnerApiRetrofitBuilder.getInstance())
                    .deleteGroup(TeamInfoLoader.getInstance().getTeamId(), topic.getId());
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