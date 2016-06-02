package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.start.Marker;

import java.sql.SQLException;

public class RoomMarkerRepository extends LockExecutorTemplate {

    private static RoomMarkerRepository instance;

    synchronized public static RoomMarkerRepository getInstance() {
        if (instance == null) {
            instance = new RoomMarkerRepository();
        }
        return instance;
    }

    public boolean updateRoomMarker(long roomId, long memberId, long lastLinkId) {
        return execute(() -> {

            try {
                Dao<Marker, Long> dao = getHelper().getDao(Marker.class);
                UpdateBuilder<Marker, Long> markerUpdateBuilder = dao.updateBuilder();
                markerUpdateBuilder.updateColumnValue("lastLinkId", lastLinkId);
                Where<Marker, Long> whereQuery = markerUpdateBuilder.where();
                whereQuery.or(whereQuery.eq("chat_id", roomId), whereQuery.eq("topic_id", roomId))
                        .and()
                        .eq("memberId", memberId);
                markerUpdateBuilder.update();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }
}
