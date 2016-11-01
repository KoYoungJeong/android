package com.tosslab.jandi.app.local.orm.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.LastMessage;
import com.tosslab.jandi.app.network.models.start.Marker;

import java.sql.SQLException;
import java.util.Collection;

public class ChatDaoImpl extends BaseDaoImpl<Chat, Long> {
    public ChatDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Chat.class);
    }

    @Override
    public int create(Chat data) throws SQLException {
        createLastMessage(data.getLastMessage());
        int id = super.create(data);
        upsertMarker(data.getId(), data.getMarkers());
        return id;
    }

    @Override
    public int update(Chat data) throws SQLException {
        createLastMessage(data.getLastMessage());
        int row = super.update(data);
        upsertMarker(data.getId(), data.getMarkers());
        return row;
    }

    private void upsertMarker(long chatId, Collection<Marker> markers) throws SQLException {
        Dao<Marker, ?> dao = DaoManager.createDao(getConnectionSource(), Marker.class);
        DeleteBuilder<Marker, ?> markerDeleteBuilder = dao.deleteBuilder();
        markerDeleteBuilder.where()
                .eq("chat_id", chatId);
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

    private void createLastMessage(LastMessage announcement) throws SQLException {
        if (announcement != null) {
            Dao<LastMessage, ?> dao = DaoManager.createDao(getConnectionSource(), LastMessage.class);
            dao.createIfNotExists(announcement);
        }
    }
}
