package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.local.orm.domain.PushHistory;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;

import java.sql.SQLException;
import java.util.List;

public class PushHistoryRepository extends LockExecutorTemplate {


    private static PushHistoryRepository repository;

    synchronized public static PushHistoryRepository getRepository() {
        if (repository == null) {
            repository = new PushHistoryRepository();
        }
        return repository;
    }

    public boolean insertPushHistory(long roomId, long messageId) {
        return execute(() -> {

            try {
                Dao<PushHistory, ?> dao = getHelper().getDao(PushHistory.class);
                return dao.create(new PushHistory(roomId, messageId)) >= 1;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean isLatestPush(long messageId) {
        return execute(() -> {
            try {
                Dao<PushHistory, ?> dao = getHelper().getDao(PushHistory.class);
                return dao.queryBuilder()
                        .where()
                        .ge("messageId", messageId)
                        .countOf() <= 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public PushHistory getLatestPushHistory() {
        return execute(() -> {
            try {
                Dao<PushHistory, ?> dao = getHelper().getDao(PushHistory.class);
                List<PushHistory> query = dao.queryBuilder()
                        .limit(1L)
                        .query();

                if (query.size() > 0) {
                    return query.get(0);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new PushHistory();
        });
    }
}
