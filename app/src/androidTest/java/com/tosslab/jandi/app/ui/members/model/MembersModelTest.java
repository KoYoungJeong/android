package com.tosslab.jandi.app.ui.members.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import dagger.Component;
import rx.Observable;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class MembersModelTest {

    @Inject
    MembersModel membersModel;

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
        DaggerMembersModelTest_TestComponent.builder().build().inject(this);
    }

    @Component(modules = ApiClientModule.class)
    interface TestComponent{
        void inject(MembersModelTest test);
    }

    @Test
    public void testRemoveMember() throws Exception {
        // Given
        long topicId = TeamInfoLoader.getInstance().getDefaultTopicId();
        List<ChatChooseItem> topicMembers = membersModel.getTopicMembers(topicId);
        long entityId = topicMembers.get(0).getEntityId();

        // When
        boolean remove = membersModel.removeMember(topicId, entityId);


        // Then
        List<ChatChooseItem> newTopicMembers = membersModel.getTopicMembers(topicId);
        assertThat(remove, is(true));
        assertThat(newTopicMembers.size(), is(lessThan(topicMembers.size())));

        TestSubscriber<ChatChooseItem> subscriber = new TestSubscriber<>();
        Observable.from(newTopicMembers)
                .filter(chatChooseItem -> chatChooseItem.getEntityId() == entityId)
                .subscribe(subscriber);

        subscriber.assertValueCount(0);
    }

    @Test
    public void testGetTopicMembers() throws Exception {
        // Given
        long defaultTopicId = TeamInfoLoader.getInstance().getDefaultTopicId();
        Collection<Long> members = TeamInfoLoader.getInstance().getTopic(defaultTopicId).getMembers();
        // When
        List<ChatChooseItem> topicMembers = membersModel.getTopicMembers(defaultTopicId);
        // Then
        assertThat(topicMembers.size(), is(equalTo(members.size())));

        for (ChatChooseItem topicMember : topicMembers) {
            if (!topicMember.isEnabled()) {
                fail("모든 멤버는 enabled 상태이어야 한다.");
            }
        }
    }

    @Test
    public void testGetTeamMembers() throws Exception {
        // Given
        int memberCnt = Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(User::isEnabled)
                .count().toBlocking().firstOrDefault(0);


        // When
        List<ChatChooseItem> result = membersModel.getTeamMembers();

        // Then
        assertThat(result.size(), is(greaterThanOrEqualTo(memberCnt)));
    }

    @Test
    public void testGetUnjoinedTopicMembers() throws Exception {
        // Given
        long defaultTopicId = TeamInfoLoader.getInstance().getDefaultTopicId();
        Collection<Long> members = TeamInfoLoader.getInstance().getTopic(defaultTopicId).getMembers();
        int memberCnt = Observable.from(TeamInfoLoader.getInstance().getUserList())
                .filter(user -> !members.contains(user.getId()))
                .filter(user -> !TeamInfoLoader.getInstance().isJandiBot(user.getId()))
                .count()
                .toBlocking()
                .firstOrDefault(0);

        // When
        List<ChatChooseItem> result = membersModel.getUnjoinedTopicMembers(defaultTopicId);
        // Then
        assertThat(memberCnt, is(equalTo(result.size())));
    }

    @Test
    public void testKickUser() throws Exception {
        //given
        BaseInitUtil.createDummyTopic();
        BaseInitUtil.inviteDummyMembers();

        long topicId = BaseInitUtil.tempTopicId;
        long teamId = TeamInfoLoader.getInstance().getTeamId();

        Collection<Long> membersBefore = TeamInfoLoader.getInstance().getTopic(topicId).getMembers();
        int beforeSize = membersBefore.size();

        //When
        membersModel.kickUser(teamId, topicId, BaseInitUtil.getUserIdByEmail(BaseInitUtil.TEST2_EMAIL));

        BaseInitUtil.refreshTeamInfo();
        Collection<Long> membersAfter = TeamInfoLoader.getInstance().getTopic(topicId).getMembers();

        BaseInitUtil.deleteDummyTopic();

        //Then
        assertThat(beforeSize - 1, is(equalTo(membersAfter.size())));
    }


    @Test
    public void testAssignToTopicOwner() throws Exception {

        // Given
        BaseInitUtil.createDummyTopic();
        BaseInitUtil.inviteDummyMembers();

        long topicId = BaseInitUtil.tempTopicId;
        long teamId = TeamInfoLoader.getInstance().getTeamId();

        // When
        long memberId = BaseInitUtil.getUserIdByEmail(BaseInitUtil.TEST3_EMAIL);
        membersModel.assignToTopicOwner(teamId, topicId, memberId);

        BaseInitUtil.refreshTeamInfo();

        // Then
        long target = BaseInitUtil.getUserIdByEmail(BaseInitUtil.TEST2_EMAIL);
        assertTrue(TeamInfoLoader.getInstance().getTopic(topicId).getCreatorId() != target);

        BaseInitUtil.deleteDummyTopic();
    }
}