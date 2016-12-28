package com.tosslab.jandi.app.ui.message.detail.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import rx.Observable;
import setup.BaseInitUtil;

import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

/**
 * Created by jsuch2362 on 15. 11. 9..
 */
@RunWith(AndroidJUnit4.class)
public class TopicDetailModelTest {

    @Inject
    TopicDetailModel topicDetailModel;

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
        DaggerTopicDetailModelTest_Component.builder()
                .build()
                .inject(this);
    }

    @Test
    public void testIsPrivateTopic() throws Exception {

        {
            long defaultTopicId = TeamInfoLoader.getInstance().getDefaultTopicId();
            boolean privateTopic = topicDetailModel.isPrivateTopic(defaultTopicId);
            assertThat(privateTopic, is(false));
        }

        {
            long privateTopicId = Observable.from(TeamInfoLoader.getInstance().getTopicList())
                    .takeFirst(topicRoom -> !topicRoom.isPublicTopic())
                    .map(TopicRoom::getId)
                    .toBlocking()
                    .firstOrDefault(-1L);
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
            long defaultTopicId = TeamInfoLoader.getInstance().getDefaultTopicId();
            boolean autoJoin = topicDetailModel.isAutoJoin(defaultTopicId);
            assertThat(autoJoin, is(true));
        }

        {
            long privateTopicId = Observable.from(TeamInfoLoader.getInstance().getTopicList())
                    .takeFirst(topicRoom -> !topicRoom.isPublicTopic())
                    .map(TopicRoom::getId)
                    .toBlocking()
                    .firstOrDefault(-1L);
            boolean autoJoin = topicDetailModel.isAutoJoin(privateTopicId);
            assertThat(autoJoin, is(false));
        }

        {
            try {
                topicDetailModel.isAutoJoin(-1);
                fail("It cannot occured");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Test
    public void testGetEnabledTeamMemberCount() throws Exception {

        int enabledTeamMemberCount = topicDetailModel.getEnabledTeamMemberCount();

        int size = TeamInfoLoader.getInstance().getUserList().size();

        assertThat(enabledTeamMemberCount, is(lessThanOrEqualTo(size)));

        int memberCount = TeamInfoLoader.getInstance()
                .getTopic(TeamInfoLoader.getInstance().getDefaultTopicId())
                .getMemberCount();

        assertThat(enabledTeamMemberCount, is(lessThanOrEqualTo(memberCount)));
    }

    @dagger.Component(modules = ApiClientModule.class)
    public interface Component {
        void inject(TopicDetailModelTest test);
    }
}