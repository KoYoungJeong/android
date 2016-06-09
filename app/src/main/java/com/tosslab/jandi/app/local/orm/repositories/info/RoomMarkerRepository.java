package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.network.models.start.Topic;

import java.sql.SQLException;

public class RoomMarkerRepository extends LockExecutorTemplate {

    private static RoomMarkerRepository instance;

    synchronized public static RoomMarkerRepository getInstance() {
        if (instance == null) {
            instance = new RoomMarkerRepository();
        }
        return instance;
    }

    public boolean upsertRoomMarker(long roomId, long memberId, long lastLinkId) {
        return execute(() -> {

            try {
                Dao<Marker, Long> dao = getHelper().getDao(Marker.class);

                UpdateBuilder<Marker, Long> markerUpdateBuilder = dao.updateBuilder();
                markerUpdateBuilder.updateColumnValue("readLinkId", lastLinkId);
                Where<Marker, Long> whereQuery = markerUpdateBuilder.where();
                whereQuery.or(whereQuery.eq("chat_id", roomId), whereQuery.eq("topic_id", roomId))
                        .and()
                        .eq("memberId", memberId);
                if (markerUpdateBuilder.update() <= 0) {
                    Marker newMarker = new Marker();
                    if (TopicRepository.getInstance().hasTopic(roomId)) {
                        Topic topic = TopicRepository.getInstance().getTopic(roomId);
                        newMarker.setTopic(topic);

                    } else {
                        Chat chat = ChatRepository.getInstance().getChat(roomId);
                        newMarker.setChat(chat);
                    }
                    newMarker.setMemberId(memberId);
                    newMarker.setReadLinkId(lastLinkId);
                    return dao.create(newMarker) > 0;
                } else {
                    return true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public Marker getMarker(long roomId, long memberId) {

        return execute(() -> {

            try {
                Dao<Marker, Long> dao = getHelper().getDao(Marker.class);
                Where<Marker, Long> where = dao.queryBuilder().where();
                return where.or(where.eq("chat_id", roomId), where.eq("topic_id", roomId))
                        .and()
                        .eq("memberId", memberId)
                        .queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new Marker();
        });

    }

    public long getRoomMarkerCount(long roomId, long linkId) {
        return execute(() -> {

            try {
                Dao<Marker, Long> dao = getHelper().getDao(Marker.class);
                Where<Marker, Long> where = dao.queryBuilder().where();
                return where.or(where.eq("chat_id", roomId), where.eq("topic_id", roomId))
                        .and()
                        .ge("readLinkId", 0)
                        .and()
                        .lt("readLinkId", linkId)
                        .countOf();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return 0L;
        });

    }
}
