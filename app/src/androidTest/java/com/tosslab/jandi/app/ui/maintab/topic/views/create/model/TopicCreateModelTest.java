package com.tosslab.jandi.app.ui.maintab.topic.views.create.model;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ResCommon;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import retrofit.RetrofitError;
import setup.BaseInitUtil;

import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(AndroidJUnit4.class)
public class TopicCreateModelTest {

    private static final String TAG = TopicCreateModelTest.class.getName();
    private TopicCreateModel topicCreateModel;
    ;

    @Before
    public void setUp() throws Exception {

        BaseInitUtil.initData();
        topicCreateModel = TopicCreateModel_.getInstance_(JandiApplication.getContext());
    }

    @Test
    public void testCreateTopic() throws Exception {
        {
            // When
            String topicName = "haha";
            String topicDescription = "haha2";
            ResCommon topic = topicCreateModel.createTopic(topicName, true, topicDescription, true);
            topicCreateModel.refreshEntity();

            // Then
            FormattedEntity entity = EntityManager.getInstance().getEntityById(topic.id);
            assertThat(entity, is(notNullValue()));
            assertThat(entity, is(not(EntityManager.UNKNOWN_USER_ENTITY)));
            assertThat(entity.getName(), is(equalTo(topicName)));
            assertThat(entity.getDescription(), is(equalTo(topicDescription)));
            assertThat(entity.isAutoJoin(), is(true));

            // Restore
            RequestApiManager.getInstance().deleteTopicByChannelApi(topic.id, new ReqDeleteTopic(EntityManager.getInstance().getTeamId()));
        }

        {
            // When
            String topicName = "haha";
            String topicDescription = "haha2";
            ResCommon topic = null;
            try {
                topic = topicCreateModel.createTopic(topicName, false, topicDescription, true);
                fail("절대로 성공하면 안됨");
            } catch (RetrofitError retrofitError) {
                retrofitError.printStackTrace();

                Log.d(TAG, retrofitError.getBody().toString());
            }
        }

        {
            // When
            String topicName = "haha";
            String topicDescription = "haha2";
            ResCommon topic = topicCreateModel.createTopic(topicName, false, topicDescription, false);
            topicCreateModel.refreshEntity();

            // Then
            FormattedEntity entity = EntityManager.getInstance().getEntityById(topic.id);
            assertThat(entity, is(notNullValue()));
            assertThat(entity, is(not(EntityManager.UNKNOWN_USER_ENTITY)));
            assertThat(entity.getName(), is(equalTo(topicName)));
            assertThat(entity.getDescription(), is(equalTo(topicDescription)));
            assertThat(entity.isAutoJoin(), is(false));

            // Restore
            RequestApiManager.getInstance().deleteGroupByGroupApi(EntityManager.getInstance().getTeamId(), topic.id);
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