package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.annotation.VisibleForTesting;
import android.support.v4.util.LongSparseArray;

import com.tosslab.jandi.app.local.orm.repositories.template.LockTemplate;
import com.tosslab.jandi.app.network.models.start.Announcement;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func0;

public class TopicRepository extends LockTemplate {
    private static LongSparseArray<TopicRepository> instance;

    private LongSparseArray<TopicRoom> topics;

    private TopicRepository() {
        super();
        topics = new LongSparseArray<>();
    }

    synchronized public static TopicRepository getInstance(long teamId) {
        if (instance == null) {
            instance = new LongSparseArray<>();
        }

        if (instance.indexOfKey(teamId) >= 0) {
            return instance.get(teamId);
        } else {
            TopicRepository value = new TopicRepository();
            instance.put(teamId, value);
            return value;

        }
    }

    public static TopicRepository getInstance() {
        return getInstance(TeamInfoLoader.getInstance().getTeamId());
    }

    @VisibleForTesting
    public List<Topic> getTopics() {
        return execute(() -> {
            List<Topic> rawTopics = new ArrayList<Topic>();
            int size = topics.size();
            for (int idx = 0; idx < size; idx++) {
                rawTopics.add(topics.valueAt(idx).getRaw());
            }
            return rawTopics;
        });
    }

    public List<Topic> getJoinedTopics() {
        return execute(() -> {
            List<Topic> rawTopics = new ArrayList<Topic>();
            int size = topics.size();
            for (int idx = 0; idx < size; idx++) {
                TopicRoom topicRoom = topics.valueAt(idx);
                if (topicRoom.isJoined()) {
                    rawTopics.add(topicRoom.getRaw());
                }
            }
            return rawTopics;
        });
    }

    @VisibleForTesting
    public Topic getDefaultTopic() {
        return execute(() -> {

            int size = topics.size();
            for (int idx = 0; idx < size; idx++) {
                TopicRoom topicRoom = topics.valueAt(idx);
                if (topicRoom.isDefaultTopic()) {
                    return topicRoom.getRaw();
                }
            }
            return null;
        });
    }

    public boolean updateStarred(long roomId, boolean starred) {
        return execute(() -> {


            if (isTopic(roomId)) {
                topics.get(roomId).getRaw().setIsStarred(starred);
                return true;
            }

            return false;
        });
    }

    public boolean deleteTopic(long topicId) {
        return execute(() -> {

            topics.remove(topicId);
            return true;

        });
    }

    public boolean updatePushSubscribe(long topicId, boolean pushSubscribe) {
        return execute(() -> {

            if (isTopic(topicId)) {
                topics.get(topicId).getRaw().setSubscribe(pushSubscribe);
                return true;
            }

            return false;
        });
    }

    public boolean updateTopicJoin(long topicId, boolean join) {
        return execute(() -> {

            if (isTopic(topicId)) {
                topics.get(topicId).getRaw().setIsJoined(join);
                return true;
            }

            return false;

        });
    }

    public boolean addMember(long topicId, List<Long> userIds) {
        return execute(() -> {

            if (!isTopic(topicId)) {
                return false;
            }

            Topic raw = topics.get(topicId).getRaw();
            if (raw.getMembers() != null) {
                Observable.just(raw.getMembers(), userIds)
                        .flatMap(Observable::from)
                        .distinct()
                        .collect((Func0<ArrayList<Long>>) ArrayList::new, List::add)
                        .subscribe(raw::setMembers);
            } else {
                raw.setMembers(userIds);
            }

            return true;
        });
    }

    public boolean removeMember(long topicId, long memberId) {
        return execute(() -> {

            if (isTopic(topicId)) {
                List<Long> members = topics.get(topicId).getRaw().getMembers();
                if (members != null) {
                    members.remove(memberId);
                }
            }

            return true;
        });

    }

    public boolean updateUnreadCount(long topicId, int unreadCount) {
        return execute(() -> {

            if (isTopic(topicId)) {
                topics.get(topicId).getRaw().setUnreadCount(unreadCount);
            }

            return false;
        });
    }

    public int getUnreadCount(long topicId) {
        return execute(() -> {

            if (isTopic(topicId)) {
                return topics.get(topicId).getUnreadCount();
            } else {
                return 0;
            }
        });
    }

    public boolean incrementUnreadCount(long topicId) {
        return execute(() -> {

            if (isTopic(topicId)) {
                Topic raw = topics.get(topicId).getRaw();
                raw.setUnreadCount(raw.getUnreadCount() + 1);
                return true;
            }
            return false;
        });
    }

    public boolean updateLastLinkId(long topicId, long lastLinkId) {
        return execute(() -> {


            if (isTopic(topicId)) {
                topics.get(topicId).getRaw().setLastLinkId(lastLinkId);
                return true;
            }

            return false;
        });
    }

    public boolean updateDescription(long topicId, String description) {
        return execute(() -> {

            if (isTopic(topicId)) {
                topics.get(topicId).getRaw().setDescription(description);
                return true;
            }

            return false;

        });
    }

    public boolean updateName(long topicId, String topicName) {
        return execute(() -> {


            if (isTopic(topicId)) {
                topics.get(topicId).getRaw().setName(topicName);
                return true;
            }

            return false;
        });

    }

    public boolean updateAutoJoin(long topicId, boolean autoJoin) {
        return execute(() -> {


            if (isTopic(topicId)) {
                topics.get(topicId).getRaw().setAutoJoin(autoJoin);
                return true;
            }

            return false;

        });

    }

    public boolean createAnnounce(long topicId, Announcement announcement) {
        return execute(() -> {

            if (isTopic(topicId)) {
                topics.get(topicId).getRaw().setAnnouncement(announcement);
                return true;
            }
            return false;
        });
    }

    public boolean removeAnnounce(long topicId) {
        return execute(() -> {

            if (isTopic(topicId)) {
                topics.get(topicId).getRaw().setAnnouncement(null);
                return true;
            }
            return false;
        });

    }

    public boolean addTopic(Topic topic) {
        return execute(() -> {

            if (!isTopic(topic.getId())) {
                topics.put(topic.getId(), new TopicRoom(topic));
            }

            return true;
        });
    }

    public boolean updateTopic(Topic topic) {
        return execute(() -> {


            if (isTopic(topic.getId())) {

                Topic savedTopic = topics.get(topic.getId()).getRaw();

                savedTopic.setType(topic.getType());
                savedTopic.setName(topic.getName());
                savedTopic.setStatus(topic.getStatus());
                savedTopic.setDescription(topic.getDescription());
                savedTopic.setIsDefault(topic.isDefault());
                savedTopic.setAutoJoin(topic.isAutoJoin());
                savedTopic.setIsAnnouncement(topic.isAnnouncement());
                savedTopic.setAnnouncement(topic.getAnnouncement());
                savedTopic.setCreatorId(topic.getCreatorId());
                savedTopic.setLastLinkId(topic.getLastLinkId());

                Observable.just(savedTopic.getMembers(), topic.getMembers())
                        .flatMap(Observable::from)
                        .distinct()
                        .collect((Func0<ArrayList<Long>>) ArrayList::new, List::add)
                        .subscribe(savedTopic::setMembers);
            } else {
                topics.put(topic.getId(), new TopicRoom(topic));
            }

            return true;
        });
    }

    public boolean updateAnnounceOpened(long topicId, boolean opened) {
        return execute(() -> {

            if (isTopic(topicId)) {
                Announcement announcement = topics.get(topicId).getRaw().getAnnouncement();
                if (announcement != null) {
                    announcement.setIsOpened(opened);
                    return true;
                }
            }

            return false;
        });

    }

    public boolean isTopic(long topicId) {
        return execute(() -> topics.indexOfKey(topicId) >= 0);
    }

    public Topic getTopic(long roomId) {
        return execute(() -> {
            if (isTopic(roomId)) {
                return topics.get(roomId).getRaw();
            }
            return null;
        });
    }

    public boolean updateReadLinkId(long roomId, long linkId) {
        return execute(() -> {

            if (isTopic(roomId)) {
                Topic raw = topics.get(roomId).getRaw();
                if (raw.getReadLinkId() < linkId) {
                    raw.setReadLinkId(linkId);
                }
                return true;
            }

            return false;
        });
    }

    public boolean updateReadOnly(long roomId, boolean readOnly) {
        return execute(() -> {

            if (isTopic(roomId)) {
                topics.get(roomId).getRaw().setIsAnnouncement(readOnly);
                return true;
            }
            return false;
        });
    }

    public boolean clear() {
        return execute(() -> {
            topics.clear();
            return true;
        });
    }

    public boolean addTopicRoom(long topicId, TopicRoom topic) {
        return execute(() -> {
            topics.put(topicId, topic);
            return true;
        });
    }

    public TopicRoom getTopicRoom(long roomId) {
        return execute(() -> topics.get(roomId));
    }

    public List<TopicRoom> getTopicRooms() {
        return execute(() -> {
            List<TopicRoom> topicRooms = new ArrayList<>();

            int size = topics.size();
            for (int idx = 0; idx < size; idx++) {
                topicRooms.add(topics.valueAt(idx));
            }
            return topicRooms;
        });
    }
}
