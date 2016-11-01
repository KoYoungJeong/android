package com.tosslab.jandi.app.local.orm.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.start.Announcement;
import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.network.models.start.Topic;

import java.sql.SQLException;
import java.util.Collection;

public class TopicDaoImpl extends BaseDaoImpl<Topic, Long> {
    public TopicDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Topic.class);
    }

    @Override
    public int create(Topic data) throws SQLException {
        int id = super.create(data);
        createAnnouncement(data.getAnnouncement());
        upsertMarker(data.getId(), data.getMarkers());
        return id;
    }

    @Override
    public int update(Topic data) throws SQLException {
        int row = super.update(data);
        createAnnouncement(data.getAnnouncement());
        upsertMarker(data.getId(), data.getMarkers());
        return row;
    }

    private void upsertMarker(long topicId, Collection<Marker> markers) throws SQLException {
        Dao<Marker, ?> dao = DaoManager.createDao(getConnectionSource(), Marker.class);
        DeleteBuilder<Marker, ?> markerDeleteBuilder = dao.deleteBuilder();
        markerDeleteBuilder.where()
                .eq("topic_id", topicId);
        markerDeleteBuilder.delete();

        if (markers != null && !markers.isEmpty()) {
            try {
                dao.callBatchTasks(() -> {
                    for (Marker marker : markers) {
                        dao.create(marker);
                    }
                    return null;
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void createAnnouncement(Announcement announcement) throws SQLException {
        if (announcement != null) {
            Dao<Announcement, ?> dao = DaoManager.createDao(getConnectionSource(), Announcement.class);
            dao.createIfNotExists(announcement);
        }
    }
}
