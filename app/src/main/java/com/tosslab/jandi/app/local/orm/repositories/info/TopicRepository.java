package com.tosslab.jandi.app.local.orm.repositories.info;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.realm.RealmRepository;
import com.tosslab.jandi.app.network.models.start.Announcement;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.start.RealmLong;
import com.tosslab.jandi.app.network.models.start.Topic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.RealmList;
import io.realm.RealmResults;

public class TopicRepository extends RealmRepository {
    private static TopicRepository instance;

    synchronized public static TopicRepository getInstance() {
        if (instance == null) {
            instance = new TopicRepository();
        }
        return instance;
    }

    public List<Topic> getTopics(long teamId) {
        return execute((realm) -> {
            RealmResults<Topic> it = realm.where(Topic.class)
                    .equalTo("teamId", teamId)
                    .findAll();
            if (it != null && !it.isEmpty()) {
                return realm.copyFromRealm(it);
            } else {
                return new ArrayList<Topic>();
            }
        });
    }

    public List<Topic> getJoinedTopics(long teamId) {
        return execute(realm -> {
            RealmResults<Topic> it = realm.where(Topic.class)
                    .equalTo("teamId", teamId)
                    .equalTo("isJoined", true)
                    .findAll();
            if (it != null && !it.isEmpty()) {
                return realm.copyFromRealm(it);
            } else {
                return new ArrayList<Topic>();
            }
        });
    }

    public Topic getDefaultTopic(long teamId) {
        return execute((realm) -> {
            Topic it = realm.where(Topic.class)
                    .equalTo("teamId", teamId)
                    .equalTo("isDefault", true)
                    .findFirst();
            if (it != null) {
                return realm.copyFromRealm(it);
            } else {
                return null;
            }
        });
    }

    public boolean updateStarred(long roomId, boolean starred) {
        return execute((realm) -> {

            Topic topic = realm.where(Topic.class)
                    .equalTo("id", roomId)
                    .findFirst();

            if (topic != null) {
                realm.executeTransaction(realm1 -> topic.setIsStarred(starred));
                return true;
            }

            return false;
        });
    }

    public boolean deleteTopic(long topicId) {
        return execute((realm) -> {

            Topic topic = realm.where(Topic.class)
                    .equalTo("id", topicId)
                    .findFirst();
            realm.executeTransaction(realm1 -> topic.deleteFromRealm());

            return true;
        });
    }

    public boolean updatePushSubscribe(long topicId, boolean pushSubscribe) {
        return execute((realm) -> {

            Topic topic = realm.where(Topic.class)
                    .equalTo("id", topicId)
                    .findFirst();
            if (topic != null) {
                realm.executeTransaction(realm1 -> topic.setSubscribe(pushSubscribe));
                return true;
            }

            return false;
        });
    }

    public boolean updateTopicJoin(long topicId, boolean join) {
        return execute((realm) -> {

            Topic topic = realm.where(Topic.class)
                    .equalTo("id", topicId)
                    .findFirst();
            if (topic != null) {
                realm.executeTransaction(realm1 -> topic.setIsJoined(join));
                return true;
            }

            return false;

        });
    }

    public boolean addMember(long topicId, List<Long> userIds) {
        return execute((realm) -> {

            Topic topic = realm.where(Topic.class)
                    .equalTo("id", topicId)
                    .findFirst();
            if (topic == null) {
                return false;
            }

            realm.executeTransaction(realm1 -> {

                if (topic.getMemberIds() != null) {
                    RealmList<RealmLong> memberIds = topic.getMemberIds();
                    Set<Long> inIds = new HashSet<>();
                    for (RealmLong memberId : memberIds) {
                        inIds.add(memberId.getValue());
                    }

                    for (long userId : userIds) {
                        if (!inIds.contains(userId)) {
                            RealmLong object = new RealmLong();
                            object.setValue(userId);
                            memberIds.add(object);
                        }
                    }

                } else {

                    RealmList<RealmLong> memberIds = new RealmList<>();
                    for (long userId : userIds) {
                        RealmLong object = new RealmLong();
                        object.setValue(userId);
                        memberIds.add(object);
                    }

                    topic.setMemberIds(memberIds);
                }
            });

            return true;
        });
    }

    public boolean removeMember(long topicId, long memberId) {
        return execute((realm) -> {

            Topic topic = realm.where(Topic.class).equalTo("id", topicId).findFirst();
            if (topic == null) {
                return false;
            }

            RealmList<RealmLong> memberIds = topic.getMemberIds();

            if (memberIds == null || memberIds.isEmpty()) {
                return true;
            }

            realm.executeTransaction(realm1 -> {
                for (int idx = memberIds.size() - 1; idx >= 0; idx--) {
                    if (memberIds.get(idx).getValue() == memberId) {
                        memberIds.remove(idx);
                        break;
                    }
                }
            });

            return true;
        });

    }

    public boolean updateUnreadCount(long topicId, int unreadCount) {
        return execute((realm) -> {

            Topic topic = realm.where(Topic.class)
                    .equalTo("id", topicId)
                    .findFirst();
            if (topic != null) {
                realm.executeTransaction(realm1 -> topic.setUnreadCount(unreadCount));
                return true;
            }

            return false;
        });
    }

    public int getUnreadCount(long topicId) {
        return execute((realm) -> {
            Topic topic = realm.where(Topic.class)
                    .equalTo("id", topicId)
                    .findFirst();
            if (topic != null) {
                return topic.getUnreadCount();
            }
            return -1;
        });
    }

    public boolean incrementUnreadCount(long topicId) {
        return execute((realm) -> {

            Topic topic = realm.where(Topic.class)
                    .equalTo("id", topicId)
                    .findFirst();

            if (topic != null) {
                realm.executeTransaction(realm1 -> topic.setUnreadCount(topic.getUnreadCount() + 1));
                return true;
            }
            return false;
        });
    }

    public boolean updateLastLinkId(long topicId, long lastLinkId) {
        return execute((realm) -> {


            Topic topic = realm.where(Topic.class)
                    .equalTo("id", topicId)
                    .findFirst();

            if (topic != null && topic.getLastLinkId() < lastLinkId) {
                realm.executeTransaction(realm1 -> topic.setLastLinkId(lastLinkId));
                return true;
            }

            return false;
        });
    }

    public boolean updateDescription(long topicId, String description) {
        return execute((realm) -> {


            Topic topic = realm.where(Topic.class)
                    .equalTo("id", topicId)
                    .findFirst();

            if (topic != null) {
                realm.executeTransaction(realm1 -> topic.setDescription(description));
                return true;
            }
            return false;

        });
    }

    public boolean updateName(long topicId, String topicName) {
        return execute((realm) -> {


            Topic topic = realm.where(Topic.class)
                    .equalTo("id", topicId)
                    .findFirst();

            if (topic != null) {
                realm.executeTransaction(realm1 -> topic.setName(topicName));
                return true;
            }

            return false;
        });

    }

    public boolean updateAutoJoin(long topicId, boolean autoJoin) {
        return execute((realm) -> {

            Topic topic = realm.where(Topic.class)
                    .equalTo("id", topicId)
                    .findFirst();

            if (topic != null) {
                realm.executeTransaction(realm1 -> topic.setAutoJoin(autoJoin));
                return true;
            }

            return false;

        });

    }

    public boolean createAnnounce(long topicId, Announcement announcement) {
        return execute((realm) -> {

            Topic topic = realm.where(Topic.class)
                    .equalTo("id", topicId)
                    .findFirst();

            if (topic == null) {
                return false;
            }

            announcement.setRoomId(topicId);
            realm.executeTransaction(realm1 -> {
                Announcement copied = realm.copyToRealmOrUpdate(announcement);
                topic.setAnnouncement(copied);
            });
            return true;
        });
    }

    public boolean removeAnnounce(long topicId) {
        return execute((realm) -> {

            Topic topic = realm.where(Topic.class)
                    .equalTo("id", topicId)
                    .findFirst();
            if (topic != null && topic.getAnnouncement() != null) {
                realm.executeTransaction(realm1 -> {
                    topic.getAnnouncement().deleteFromRealm();
                    topic.setAnnouncement(null);
                });
                return true;
            }
            return false;
        });

    }

    public boolean addTopic(Topic topic) {
        return execute((realm) -> {
            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            InitialInfo initialInfo = realm.where(InitialInfo.class)
                    .equalTo("teamId", selectedTeamId)
                    .findFirst();
            if (initialInfo != null) {
                realm.executeTransaction(realm1 -> {
                    topic.setTeamId(selectedTeamId);
                    initialInfo.getTopics().add(topic);
                });
            }
            return true;
        });
    }

    public boolean updateTopic(Topic topic) {
        return execute((realm) -> {

            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            topic.setTeamId(selectedTeamId);
            realm.executeTransaction(realm1 -> realm.copyToRealmOrUpdate(topic));

            return true;
        });
    }

    public boolean updateAnnounceOpened(long topicId, boolean opened) {
        return execute((realm) -> {

            Topic topic = realm.where(Topic.class)
                    .equalTo("id", topicId)
                    .findFirst();

            if (topic.getAnnouncement() != null) {
                realm.executeTransaction(realm1 -> topic.getAnnouncement().setIsOpened(opened));
                return true;
            }

            return false;
        });

    }

    public boolean isTopic(long topicId) {
        return execute((realm) -> realm.where(Topic.class)
                .equalTo("id", topicId)
                .count() > 0);
    }

    public Topic getTopic(long roomId) {
        return execute((realm) -> {
            Topic it = realm.where(Topic.class)
                    .equalTo("id", roomId)
                    .findFirst();
            if (it != null) {
                return realm.copyFromRealm(it);
            } else {
                return it;
            }
        });
    }

    public boolean updateReadLinkId(long roomId, long linkId) {
        return execute((realm) -> {

            Topic topic = realm.where(Topic.class)
                    .equalTo("id", roomId)
                    .findFirst();
            if (topic != null && topic.getReadLinkId() < linkId) {
                realm.executeTransaction(realm1 -> topic.setReadLinkId(linkId));
                return true;
            }

            return false;
        });
    }
}
