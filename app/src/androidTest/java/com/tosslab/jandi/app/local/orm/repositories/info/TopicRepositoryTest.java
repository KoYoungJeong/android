package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.start.Announcement;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.start.RealmLong;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


@org.junit.runner.RunWith(AndroidJUnit4.class)
public class TopicRepositoryTest {

    private static InitialInfo initializeInfo;
    private long selectedTeamId;
    private long defaultTopicId;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
        initializeInfo = new StartApi(RetrofitBuilder.getInstance()).getInitializeInfo(TeamInfoLoader.getInstance().getTeamId());
    }

    @Before
    public void setUp() throws Exception {
        Realm.getDefaultInstance().executeTransaction(realm -> realm.deleteAll());
        InitialInfoRepository.getInstance().upsertInitialInfo(initializeInfo);
        TeamInfoLoader.getInstance().refresh();
        selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
        defaultTopicId = TeamInfoLoader.getInstance().getDefaultTopicId();
    }

    @Test
    public void testGetTopics() throws Exception {
        List<Topic> topics = TopicRepository.getInstance().getTopics(selectedTeamId);
        assertThat(topics.size()).isGreaterThan(1);
    }

    @Test
    public void testGetDefaultTopic() throws Exception {
        TopicRoom topic = TeamInfoLoader.getInstance().getTopic(defaultTopicId);

        Topic defaultTopic = TopicRepository.getInstance().getDefaultTopic(selectedTeamId);

        assertThat(defaultTopic.getName()).isEqualTo(topic.getName());
        assertThat(defaultTopic.getDescription()).isEqualTo(topic.getDescription());
        assertThat(defaultTopic.isAutoJoin()).isEqualTo(topic.isAutoJoin());
        assertThat(defaultTopic.getMemberIds().size()).isEqualTo(topic.getMemberCount());
        assertThat(defaultTopic.getUnreadCount()).isEqualTo(topic.getUnreadCount());
    }

    @Test
    public void testUpdateStarred() throws Exception {
        boolean success = TopicRepository.getInstance().updateStarred(defaultTopicId, true);
        assertThat(success).isTrue();

        Topic topic = TopicRepository.getInstance().getTopic(defaultTopicId);
        assertThat(topic.isStarred()).isTrue();

    }

    @Test
    public void testDeleteTopic() throws Exception {
        Topic originTopic = TopicRepository.getInstance().getTopic(defaultTopicId);
        boolean success = TopicRepository.getInstance().deleteTopic(defaultTopicId);

        assertThat(success).isTrue();
        assertThat(TopicRepository.getInstance().isTopic(defaultTopicId)).isFalse();

    }

    @Test
    public void testUpdatePushSubscribe() throws Exception {
        boolean success = TopicRepository.getInstance().updatePushSubscribe(defaultTopicId, false);

        assertThat(success).isTrue();
        Topic topic = TopicRepository.getInstance().getTopic(defaultTopicId);
        assertThat(topic.isSubscribe()).isFalse();

    }

    @Test
    public void testUpdateTopicJoin() throws Exception {
        boolean success = TopicRepository.getInstance().updateTopicJoin(defaultTopicId, false);
        assertThat(success).isTrue();

        Topic topic = TopicRepository.getInstance().getTopic(defaultTopicId);
        assertThat(topic.isJoined()).isFalse();

    }

    @Test
    public void testAddMember() throws Exception {
        long tempMemberId = 1l;
        boolean success = TopicRepository.getInstance().addMember(defaultTopicId, Arrays.asList(tempMemberId));
        assertThat(success).isTrue();

        Topic topic = TopicRepository.getInstance().getTopic(defaultTopicId);
        boolean contains = false;
        for (RealmLong realmLong : topic.getMemberIds()) {
            if (realmLong.getValue() == tempMemberId) {
                contains = true;
            }
        }
        assertThat(contains).isTrue();

    }

    @Test
    public void testRemoveMember() throws Exception {
        boolean success = TopicRepository.getInstance().removeMember(defaultTopicId, TeamInfoLoader.getInstance().getMyId());
        assertThat(success).isTrue();

        Topic topic = TopicRepository.getInstance().getTopic(defaultTopicId);
        boolean contains = false;
        for (RealmLong realmLong : topic.getMemberIds()) {
            if (realmLong.getValue() == TeamInfoLoader.getInstance().getMyId()) {
                contains = true;
                fail("It couldn't be");
            }
        }
    }

    @Test
    public void testUpdateUnreadCount() throws Exception {
        int unreadCount = 1;
        boolean success = TopicRepository.getInstance().updateUnreadCount(defaultTopicId, unreadCount);
        assertThat(success).isTrue();

        int unreadCount1 = TopicRepository.getInstance().getTopic(defaultTopicId).getUnreadCount();
        assertThat(unreadCount1).isEqualTo(unreadCount);

    }

    @Test
    public void testIncrementUnreadCount() throws Exception {
        long defaultTopicId = TeamInfoLoader.getInstance().getDefaultTopicId();
        TopicRoom topic = TeamInfoLoader.getInstance().getTopic(defaultTopicId);
        int unreadCount = topic.getUnreadCount();
        TopicRepository.getInstance().incrementUnreadCount(defaultTopicId);
        int newUnreadCount = TopicRepository.getInstance().getTopic(defaultTopicId).getUnreadCount();

        assertThat(newUnreadCount).isGreaterThan(unreadCount);
    }

    @Test
    public void testUpdateLastLinkId() throws Exception {
        long lastLinkId = TeamInfoLoader.getInstance().getTopic(defaultTopicId).getLastLinkId() + 1;
        boolean success = TopicRepository.getInstance().updateLastLinkId(defaultTopicId, lastLinkId);
        assertThat(success).isTrue();

        long lastLinkId1 = TopicRepository.getInstance().getTopic(defaultTopicId).getLastLinkId();
        assertThat(lastLinkId1).isEqualTo(lastLinkId);

    }

    @Test
    public void testUpdateDescription() throws Exception {
        String description = "hello world";
        boolean success = TopicRepository.getInstance().updateDescription(defaultTopicId, description);
        assertThat(success).isTrue();

        Topic topic = TopicRepository.getInstance().getTopic(defaultTopicId);
        assertThat(topic.getDescription()).isEqualToIgnoringCase(description);

    }

    @Test
    public void testUpdateName() throws Exception {
        String topicName = "hello world";
        boolean success = TopicRepository.getInstance().updateName(defaultTopicId, topicName);

        assertThat(success).isTrue();
        Topic topic = TopicRepository.getInstance().getTopic(defaultTopicId);
        assertThat(topic.getName()).isEqualToIgnoringCase(topicName);
    }

    @Test
    public void testUpdateAutoJoin() throws Exception {
        boolean autoJoin = false;
        boolean success = TopicRepository.getInstance().updateAutoJoin(defaultTopicId, autoJoin);
        assertThat(success).isTrue();

        Topic topic = TopicRepository.getInstance().getTopic(defaultTopicId);
        assertThat(topic.isAutoJoin()).isEqualTo(autoJoin);
    }

    @Test
    public void testCreateAnnounce() throws Exception {
        Announcement announce = getAnnouncement();

        boolean success = TopicRepository.getInstance().createAnnounce(defaultTopicId, announce);
        assertThat(success).isTrue();

        Announcement announcement = TopicRepository.getInstance().getTopic(defaultTopicId).getAnnouncement();
        assertThat(announcement.isOpened()).isEqualTo(announce.isOpened());
        assertThat(announcement.getCreatedAt()).isEqualTo(announce.getCreatedAt());
        assertThat(announcement.getCreatorId()).isEqualTo(announce.getCreatorId());
        assertThat(announcement.getMessageId()).isEqualTo(announce.getMessageId());
        assertThat(announcement.getWriterId()).isEqualTo(announce.getWriterId());
        assertThat(announcement.getWrittenAt()).isEqualTo(announce.getWrittenAt());

        TopicRepository.getInstance().removeAnnounce(defaultTopicId);
    }

    @NonNull
    private Announcement getAnnouncement() {
        Announcement announce = new Announcement();
        announce.setIsOpened(true);
        announce.setContent("hahahahah");
        announce.setCreatedAt(new Date());
        announce.setCreatorId(1l);
        announce.setMessageId(2l);
        announce.setWriterId(3l);
        announce.setWrittenAt(new Date());
        return announce;
    }

    @Test
    public void testRemoveAnnounce() throws Exception {
        Announcement announcement = getAnnouncement();
        TopicRepository.getInstance().createAnnounce(defaultTopicId, announcement);

        boolean success = TopicRepository.getInstance().removeAnnounce(defaultTopicId);

        assertThat(success).isTrue();

        Topic topic = TopicRepository.getInstance().getTopic(defaultTopicId);

        assertThat(topic.getAnnouncement()).isNull();
    }

    @Test
    public void testAddTopic() throws Exception {
        Topic topic = new Topic();
        topic.setTeamId(selectedTeamId);
        topic.setName("hello");
        topic.setId(1l);
        TopicRepository.getInstance().addTopic(topic);

        Topic topic1 = TopicRepository.getInstance().getTopic(1l);
        assertThat(topic1.getId()).isEqualTo(topic.getId());
        assertThat(topic1.getName()).isEqualTo(topic.getName());
    }

    @Test
    public void testUpdateTopic() throws Exception {
        Topic topic = null;
        for (Topic t : initializeInfo.getTopics()) {
            if (t.getId() == defaultTopicId) {
                topic =t;
                break;
            }
        }
        topic.setName("hellow");
        boolean success = TopicRepository.getInstance().updateTopic(topic);

        Topic topic1 = TopicRepository.getInstance().getTopic(defaultTopicId);
        assertThat(success).isTrue();
        assertThat(topic1.getName()).isEqualTo(topic.getName());

    }

    @Test
    public void testUpdateAnnounceOpened() throws Exception {
        TopicRepository.getInstance().createAnnounce(defaultTopicId, getAnnouncement());
        boolean success = TopicRepository.getInstance().updateAnnounceOpened(defaultTopicId, false);

        Topic topic = TopicRepository.getInstance().getTopic(defaultTopicId);
        assertThat(success).isTrue();
        assertThat(topic.getAnnouncement().isOpened()).isFalse();

        TopicRepository.getInstance().removeAnnounce(defaultTopicId);
    }

    @Test
    public void testIsTopic() throws Exception {
        assertThat(TopicRepository.getInstance().isTopic(1l)).isFalse();
        assertThat(TopicRepository.getInstance().isTopic(defaultTopicId)).isTrue();
    }

    @Test
    public void testGetTopic() throws Exception {
        Topic topic = TopicRepository.getInstance().getTopic(defaultTopicId);
        assertThat(topic).isNotNull();
    }

    @Test
    public void testUpdateReadId() throws Exception {

        assertThat(TopicRepository.getInstance().updateReadId(defaultTopicId, TopicRepository.getInstance().getTopic(defaultTopicId).getReadLinkId() - 1)).isFalse();

        long newReadId = TopicRepository.getInstance().getTopic(defaultTopicId).getReadLinkId() + 1;
        assertThat(TopicRepository.getInstance().updateReadId(defaultTopicId, newReadId)).isTrue();
        assertThat(TopicRepository.getInstance().getTopic(defaultTopicId).getReadLinkId()).isEqualTo(newReadId);
    }
}