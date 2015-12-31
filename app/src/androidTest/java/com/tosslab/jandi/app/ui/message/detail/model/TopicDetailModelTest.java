package com.tosslab.jandi.app.ui.message.detail.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

/**
 * Created by jsuch2362 on 15. 11. 9..
 */
@RunWith(AndroidJUnit4.class)
public class TopicDetailModelTest {

    private TopicDetailModel topicDetailModel;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();

        topicDetailModel = TopicDetailModel_.getInstance_(JandiApplication.getContext());
    }

    @Test
    public void testIsPrivateTopic() throws Exception {

        {
            int defaultTopicId = EntityManager.getInstance().getDefaultTopicId();
            boolean privateTopic = topicDetailModel.isPrivateTopic(defaultTopicId);
            assertThat(privateTopic, is(false));
        }

        {
            int privateTopicId = EntityManager.getInstance().getGroups().get(0).getId();
            boolean privateTopic = topicDetailModel.isPrivateTopic(privateTopicId);
            assertThat(privateTopic, is(true));
        }

        {
            boolean privateTopic = topicDetailModel.isPrivateTopic(-1);
            assertThat(privateTopic, is(false));
        }
    }

    @Test
    public void testIsAutoJoin() throws Exception {
        {
            int defaultTopicId = EntityManager.getInstance().getDefaultTopicId();
            boolean autoJoin = topicDetailModel.isAutoJoin(defaultTopicId);
            assertThat(autoJoin, is(true));
        }

        {
            int privateTopicId = EntityManager.getInstance().getGroups().get(0).getId();
            boolean autoJoin = topicDetailModel.isAutoJoin(privateTopicId);
            assertThat(autoJoin, is(false));
        }

        {
            boolean autoJoin = topicDetailModel.isAutoJoin(-1);
            assertThat(autoJoin, is(false));
        }

    }

    @Test
    public void testGetEnabledTeamMemberCount() throws Exception {

        int enabledTeamMemberCount = topicDetailModel.getEnabledTeamMemberCount();

        EntityManager entityManager = EntityManager.getInstance();
        int size = entityManager.getFormattedUsers().size();

        assertThat(enabledTeamMemberCount, is(lessThanOrEqualTo(size)));

        int memberCount = entityManager.getEntityById(entityManager.getDefaultTopicId()).getMemberCount();

        assertThat(enabledTeamMemberCount, is(lessThanOrEqualTo(memberCount)));
    }
}