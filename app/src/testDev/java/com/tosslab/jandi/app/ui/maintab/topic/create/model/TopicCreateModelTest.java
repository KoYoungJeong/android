package com.tosslab.jandi.app.ui.maintab.topic.create.model;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;

import rx.Observable;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Steve SeongUg Jung on 15. 7. 6..
 */
@RunWith(RobolectricGradleTestRunner.class)
public class TopicCreateModelTest {

    private TopicCreateModel topicCreateModel;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(Robolectric.application);

        int teamId = AccountRepository.getRepository().getAccountTeams().get(0).getTeamId();
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);

        topicCreateModel = TopicCreateModel_.getInstance_(Robolectric.application);
    }

    @Test
    public void testCreateTopic() throws Exception {

        String tempName = "Topic Create Test Name";
        String tempDescription = "tempDescription";

        ResCommon topic = topicCreateModel.createTopic(tempName, false, tempDescription);
        assertTrue(topic.id > 0);

        int createdId = topic.id;

        ResLeftSideMenu totalEntitiesInfo = EntityClientManager_
                .getInstance_(Robolectric.application)
                .getTotalEntitiesInfo();

        ResLeftSideMenu.Entity defaultValue = new ResLeftSideMenu.Entity();
        ResLeftSideMenu.Entity createdEntity = Observable.from(totalEntitiesInfo.joinEntities)
                .filter(entity -> entity.id == createdId)
                .firstOrDefault(defaultValue)
                .toBlocking().first();

        EntityClientManager_
                .getInstance_(Robolectric.application)
                .deletePrivateGroup(createdId);

        if (createdEntity == defaultValue) {
            fail(createdId + " : Cannot find created private entity");
        }

        String description = ((ResLeftSideMenu.PrivateGroup) createdEntity).description;

        assertThat(description, is(tempDescription));

    }

    @Test
    public void testCreatePublicTopic() throws Exception {

        String tempName = "Topic Create Test Name";
        String tempDescription = "tempDescription";

        ResCommon topic = topicCreateModel.createTopic(tempName, true, tempDescription);

        assertTrue(topic.id > 0);

        int createdId = topic.id;

        ResLeftSideMenu totalEntitiesInfo = EntityClientManager_
                .getInstance_(Robolectric.application)
                .getTotalEntitiesInfo();

        ResLeftSideMenu.Entity defaultValue = new ResLeftSideMenu.Entity();
        ResLeftSideMenu.Entity createdEntity = Observable.from(totalEntitiesInfo.joinEntities)
                .filter(entity -> entity.id == createdId)
                .firstOrDefault(defaultValue)
                .toBlocking().first();

        EntityClientManager_
                .getInstance_(Robolectric.application)
                .deleteChannel(createdId);

        if (createdEntity == defaultValue) {
            fail(createdId + " : Cannot find created public entity");
        }

        String description = ((ResLeftSideMenu.Channel) createdEntity).description;

        assertThat(description, is(tempDescription));

    }
}