package com.tosslab.jandi.app.ui.members.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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

/**
 * Created by tee on 15. 11. 11..
 */
@RunWith(AndroidJUnit4.class)
public class MembersModelTest {

    private MembersModel membersModel;
    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }
    @Before
    public void setUp() throws Exception {
        membersModel = MembersModel_.getInstance_(JandiApplication.getContext());
    }

    @Test
    public void testRemoveMember() throws Exception {
        // Given
        long topicId = EntityManager.getInstance().getDefaultTopicId();
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
        long defaultTopicId = EntityManager.getInstance().getDefaultTopicId();
        Collection<Long> members = EntityManager.getInstance().getEntityById(defaultTopicId).getMembers();
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
        List<FormattedEntity> formattedMembers = EntityManager.getInstance().getFormattedUsers();
        Iterator i = formattedMembers.iterator();
        int memberCnt = 0;
        while (i.hasNext()) {
            FormattedEntity f = (FormattedEntity) i.next();
            if (f.isEnabled()) {
                memberCnt++;
            }
        }

        // When
        List<ChatChooseItem> result = membersModel.getTeamMembers();

        // Then
        assertThat(result.size(), is(greaterThanOrEqualTo(memberCnt)));
    }

    @Test
    public void testGetUnjoinedTopicMembers() throws Exception {
        // Given
        long defaultTopicId = EntityManager.getInstance().getDefaultTopicId();
        List<FormattedEntity> unJoinedMember = EntityManager.getInstance()
                .getUnjoinedMembersOfEntity(defaultTopicId, JandiConstants.TYPE_PUBLIC_TOPIC);
        int memberCnt = 0;
        Iterator i = unJoinedMember.iterator();
        while (i.hasNext()) {
            FormattedEntity e = (FormattedEntity) i.next();
            if (e.isEnabled()) {
                memberCnt++;
            }
        }

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
        long teamId = EntityManager.getInstance().getTeamId();

        Collection<Long> membersBefore = EntityManager.getInstance().getEntityById(topicId).getMembers();

        //When
        membersModel.kickUser(teamId, topicId, BaseInitUtil.getUserIdByEmail(BaseInitUtil.TEST2_EMAIL));

        BaseInitUtil.refreshLeftSideMenu();
        Collection<Long> membersAfter = EntityManager.getInstance().getEntityById(topicId).getMembers();

        BaseInitUtil.deleteDummyTopic();

        //Then
        assertThat(membersBefore.size() - 1, is(equalTo(membersAfter.size())));
    }


    @Test
    public void testAssignToTopicOwner() throws Exception {

        // Given
        BaseInitUtil.createDummyTopic();
        BaseInitUtil.inviteDummyMembers();

        long topicId = BaseInitUtil.tempTopicId;
        long teamId = EntityManager.getInstance().getTeamId();

        // When
        long memberId = BaseInitUtil.getUserIdByEmail(BaseInitUtil.TEST3_EMAIL);
        membersModel.assignToTopicOwner(teamId, topicId, memberId);

        BaseInitUtil.refreshLeftSideMenu();

        // Then
        long target = BaseInitUtil.getUserIdByEmail(BaseInitUtil.TEST2_EMAIL);
        assertTrue(!EntityManager.getInstance().isTopicOwner(topicId, target));

        BaseInitUtil.deleteDummyTopic();
    }
}