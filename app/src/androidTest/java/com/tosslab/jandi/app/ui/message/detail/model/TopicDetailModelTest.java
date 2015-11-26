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
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.IsEqual.equalTo;

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
    public void testGetEnabledTeamMemberCount() throws Exception {

        int enabledTeamMemberCount = topicDetailModel.getEnabledTeamMemberCount();

        EntityManager entityManager = EntityManager.getInstance();
        int size = entityManager.getFormattedUsers().size();

        assertThat(enabledTeamMemberCount, is(lessThan(size)));

        int memberCount = entityManager.getEntityById(entityManager.getDefaultTopicId()).getMemberCount();

        assertThat(enabledTeamMemberCount, is(equalTo(memberCount)));
    }
}