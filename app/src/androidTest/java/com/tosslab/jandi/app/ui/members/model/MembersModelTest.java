package com.tosslab.jandi.app.ui.members.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import rx.Observable;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class MembersModelTest {

    private MembersModel membersModel;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();
        membersModel = MembersModel_.getInstance_(JandiApplication.getContext());
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.clear();
    }

    @Test
    public void testRemoveMember() throws Exception {
        // Given
        int topicId = EntityManager.getInstance().getDefaultTopicId();
        List<ChatChooseItem> topicMembers = membersModel.getTopicMembers(topicId);
        int entityId = topicMembers.get(0).getEntityId();

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
}