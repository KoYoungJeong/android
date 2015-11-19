package com.tosslab.jandi.app.ui.members.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import setup.BaseInitUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

/**
 * Created by tee on 15. 11. 11..
 */
public class MembersModelTest {

    private MembersModel membersModel;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();
        membersModel = MembersModel_.getInstance_(JandiApplication.getContext());
    }

    @Test
    public void testGetTopicMembers() throws Exception {
        // Given
        int defaultTopicId = EntityManager.getInstance().getDefaultTopicId();
        Collection<Integer> members = EntityManager.getInstance().getEntityById(defaultTopicId).getMembers();
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
            if (TextUtils.equals(f.getUser().status, "enabled")) {
                memberCnt++;
            }
        }

        // When
        List<ChatChooseItem> result = membersModel.getTeamMembers();

        // Then
        assertThat(memberCnt, is(equalTo(result.size())));
    }

    @Test
    public void testGetUnjoinedTopicMembers() throws Exception {
        // Given
        int defaultTopicId = EntityManager.getInstance().getDefaultTopicId();
        List<FormattedEntity> unJoinedMember = EntityManager.getInstance()
                .getUnjoinedMembersOfEntity(defaultTopicId, JandiConstants.TYPE_PUBLIC_TOPIC);
        int memberCnt = 0;
        Iterator i = unJoinedMember.iterator();
        while (i.hasNext()) {
            FormattedEntity e = (FormattedEntity) i.next();
            if (TextUtils.equals(e.getUser().status, "enabled")) {
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

        int topicId = BaseInitUtil.tempTopicId;
        int teamId = EntityManager.getInstance().getTeamId();

        Collection<Integer> membersBefore = EntityManager.getInstance().getEntityById(topicId).getMembers();

        //When
        membersModel.kickUser(teamId, topicId, Integer.valueOf(BaseInitUtil.getUserIdByEmail("androidtester2@gustr.com")));

        BaseInitUtil.refreshLeftSideMenu();
        Collection<Integer> membersAfter = EntityManager.getInstance().getEntityById(topicId).getMembers();

        BaseInitUtil.deleteDummyTopic();

        //Then
        assertThat(membersBefore.size() - 1, is(equalTo(membersAfter.size())));

    }
}