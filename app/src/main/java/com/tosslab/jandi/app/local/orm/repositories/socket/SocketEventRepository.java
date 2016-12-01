package com.tosslab.jandi.app.local.orm.repositories.socket;


import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.tosslab.jandi.app.local.orm.domain.SocketEvent;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;

import java.sql.SQLException;

public class SocketEventRepository extends LockExecutorTemplate {
    private static SocketEventRepository instance;

    public static SocketEventRepository getInstance() {
        if (instance == null) {
            instance = new SocketEventRepository();
        }
        return instance;
    }

    public boolean addEvent(EventHistoryInfo event) {
        return execute(() -> {

            try {
                Dao<SocketEvent, String> dao = getDao(SocketEvent.class);
                dao.create(SocketEvent.createEvent(event));
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean hasEvent(EventHistoryInfo event) {
        return execute(() -> {
            try {
                Dao<SocketEvent, String> dao = getDao(SocketEvent.class);
                return dao.queryBuilder()
                        .where()
                        .eq("unique", event.getUnique())
                        .and()
                        .eq("teamId", event.getTeamId())
                        .countOf() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean deleteBeforeTime(long teamId, long time) {
        return execute(() -> {
            try {
                Dao<SocketEvent, Object> dao = getDao(SocketEvent.class);
                DeleteBuilder<SocketEvent, Object> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where()
                        .lt("ts", time)
                        .and()
                        .eq("teamId", teamId);
                return deleteBuilder.delete() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }
}
