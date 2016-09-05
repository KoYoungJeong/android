package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.start.InitialInfo;

import java.sql.SQLException;

public class InitialInfoRepository extends LockExecutorTemplate {
    private static InitialInfoRepository instance;

    synchronized public static InitialInfoRepository getInstance() {
        if (instance == null) {
            instance = new InitialInfoRepository();
        }
        return instance;
    }

    public boolean upsertInitialInfo(InitialInfo initialInfo) {
        return execute(() -> {
            try {
                Dao<InitialInfo, ?> dao = getHelper().getDao(InitialInfo.class);
                DeleteBuilder<InitialInfo, ?> deleteBuilder = dao.deleteBuilder();
                deleteBuilder.where().eq("teamId", initialInfo.getTeamId());
                deleteBuilder.delete();
                dao.createOrUpdate(initialInfo);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });

    }

    public InitialInfo getInitialInfo(long teamId) {
        return execute(() -> {
            try {
                Dao<InitialInfo, ?> dao = getHelper().getDao(InitialInfo.class);
                return dao.queryBuilder()
                        .where()
                        .eq("teamId", teamId)
                        .queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public boolean hasInitialInfo(long teamId) {
        return execute(() -> {
            try {
                Dao<InitialInfo, ?> dao = getHelper().getDao(InitialInfo.class);
                return dao.queryBuilder()
                        .where()
                        .eq("teamId", teamId)
                        .countOf() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean removeInitialInfo(long teamId) {
        return execute(() -> {
            try {
                Dao<InitialInfo, Long> dao = getHelper().getDao(InitialInfo.class);
                return dao.deleteById(teamId) > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean clear() {
        return execute(() -> {
            try {
                Dao<InitialInfo, Object> dao = getDao(InitialInfo.class);
                DeleteBuilder<InitialInfo, Object> deleteBuilder = dao.deleteBuilder();
                return deleteBuilder.delete() > 0;

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }
}
