package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.start.Topic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func0;

public class TopicRepository extends LockExecutorTemplate {
    private static TopicRepository instance;

    synchronized public static TopicRepository getInstance() {
        if (instance == null) {
            instance = new TopicRepository();
        }
        return instance;
    }

    public List<Topic> getTopics(long teamId) {
        return execute(() -> {
            try {
                Dao<Topic, ?> dao = getHelper().getDao(Topic.class);
                return dao.queryBuilder()
                        .where()
                        .eq("initialInfo_id", teamId)
                        .query();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new ArrayList<Topic>();
        });
    }

    public Topic getDefaultTopic(long teamId) {
        return execute(() -> {

            try {
                Dao<Topic, ?> dao = getHelper().getDao(Topic.class);
                return dao.queryBuilder()
                        .where()
                        .eq("initialInfo_id", teamId)
                        .and()
                        .eq("isDefault", true)
                        .queryForFirst();

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        });
    }

    public boolean updateStarred(long roomId, boolean starred) {
        return execute(() -> {
            try {
                Dao<Topic, ?> dao = getHelper().getDao(Topic.class);
                UpdateBuilder<Topic, ?> topicUpdateBuilder = dao.updateBuilder();
                topicUpdateBuilder.updateColumnValue("isStarred", starred)
                        .where()
                        .eq("id", roomId);
                return topicUpdateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean deleteTopic(long topicId) {
        return execute(() -> {
            try {
                Dao<Topic, Long> dao = getHelper().getDao(Topic.class);
                return dao.deleteById(topicId) > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean updatePushSubscribe(long topicId, boolean pushSubscribe) {
        return execute(() -> {
            try {
                Dao<Topic, ?> dao = getHelper().getDao(Topic.class);
                UpdateBuilder<Topic, ?> topicUpdateBuilder = dao.updateBuilder();
                topicUpdateBuilder.updateColumnValue("subscribe", pushSubscribe)
                        .where()
                        .eq("id", topicId);
                return topicUpdateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean updateTopicJoin(long topicId, boolean join) {
        return execute(() -> {

            try {
                Dao<Topic, ?> dao = getHelper().getDao(Topic.class);
                UpdateBuilder<Topic, ?> topicUpdateBuilder = dao.updateBuilder();
                topicUpdateBuilder.updateColumnValue("isJoined", join)
                        .where()
                        .eq("id", topicId);
                return topicUpdateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean addMember(long topicId, List<Long> userIds) {
        return execute(() -> {

            try {
                Dao<Topic, Long> dao = getHelper().getDao(Topic.class);

                Topic topic = dao.queryForId(topicId);
                ArrayList<Long> newUserIds = Observable.merge(Observable.from(topic.getMembers()), Observable.from(userIds))
                        .distinct()
                        .collect((Func0<ArrayList<Long>>) ArrayList::new, ArrayList::add)
                        .toBlocking()
                        .firstOrDefault(new ArrayList<>());
                topic.setMembers(newUserIds);

                dao.update(topic);

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean removeMember(long topicId, long memberId) {
        return execute(() -> {

            try {
                Dao<Topic, Long> dao = getHelper().getDao(Topic.class);

                Topic topic = dao.queryForId(topicId);
                ArrayList<Long> newUserIds = Observable.from(topic.getMembers())
                        .filter(it -> it != memberId)
                        .collect((Func0<ArrayList<Long>>) ArrayList::new, ArrayList::add)
                        .toBlocking()
                        .firstOrDefault(new ArrayList<>());
                topic.setMembers(newUserIds);

                dao.update(topic);

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });

    }

    public boolean updateUnreadCount(long topicId, int unreadCount) {
        return execute(() -> {
            try {
                Dao<Topic, ?> dao = getHelper().getDao(Topic.class);
                UpdateBuilder<Topic, ?> topicUpdateBuilder = dao.updateBuilder();
                topicUpdateBuilder.updateColumnValue("unreadCount", unreadCount)
                        .where()
                        .eq("id", topicId);
                return topicUpdateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean updateLastLinkId(long topicId, long lastLinkId) {
        return execute(() -> {

            try {
                Dao<Topic, ?> dao = getHelper().getDao(Topic.class);
                UpdateBuilder<Topic, ?> topicUpdateBuilder = dao.updateBuilder();
                topicUpdateBuilder.updateColumnValue("lastLinkId", lastLinkId)
                        .where()
                        .eq("id", topicId);
                return topicUpdateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean updateDescription(long topicId, String description) {
        return execute(() -> {
            try {
                Dao<Topic, ?> dao = getHelper().getDao(Topic.class);
                UpdateBuilder<Topic, ?> topicUpdateBuilder = dao.updateBuilder();
                topicUpdateBuilder.updateColumnValue("description", description)
                        .where()
                        .eq("id", topicId);
                return topicUpdateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean updateName(long topicId, String topicName) {
        return execute(() -> {
            try {
                Dao<Topic, ?> dao = getHelper().getDao(Topic.class);
                UpdateBuilder<Topic, ?> topicUpdateBuilder = dao.updateBuilder();
                topicUpdateBuilder.updateColumnValue("name", topicName)
                        .where()
                        .eq("id", topicId);
                return topicUpdateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });

    }

    public boolean updateAutoJoin(long topicId, boolean autoJoin) {
        return execute(() -> {
            try {
                Dao<Topic, ?> dao = getHelper().getDao(Topic.class);
                UpdateBuilder<Topic, ?> topicUpdateBuilder = dao.updateBuilder();
                topicUpdateBuilder.updateColumnValue("autoJoin", autoJoin)
                        .where()
                        .eq("id", topicId);
                return topicUpdateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });

    }

    public boolean createAnnounce(int topicId, Topic.Announcement announcement) {
        return execute(() -> {
            try {
                Dao<Topic.Announcement, ?> dao = getHelper().getDao(Topic.Announcement.class);
                int announceId = dao.create(announcement);
                Dao<Topic, ?> topicDao = getHelper().getDao(Topic.class);

                UpdateBuilder<Topic, ?> topicUpdateBuilder = topicDao.updateBuilder();
                topicUpdateBuilder.updateColumnValue("announcement_id", announcement.getMessageId())
                        .where()
                        .eq("id", topicId);
                int updateResult = topicUpdateBuilder.update();
                return announceId > 0 && updateResult > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean removeAnnounce(long topicId) {
        return execute(() -> {
            try {
                Dao<Topic.Announcement, Long> dao = getHelper().getDao(Topic.Announcement.class);
                Dao<Topic, Long> topicDao = getHelper().getDao(Topic.class);

                Topic topic = topicDao.queryForId(topicId);
                long messageId = topic.getAnnouncement().getMessageId();
                dao.deleteById(messageId);
                UpdateBuilder<Topic, Long> topicLongUpdateBuilder = topicDao.updateBuilder();
                topicLongUpdateBuilder.updateColumnValue("announcement_id", 0)
                        .where()
                        .eq("id", topicId);
                return topicLongUpdateBuilder.update() > 0;

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });

    }
}
