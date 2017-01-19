package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.start.RawInitialInfo;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InitialInfoRepository extends LockExecutorTemplate {
    private static InitialInfoRepository instance;

    synchronized public static InitialInfoRepository getInstance() {
        if (instance == null) {
            instance = new InitialInfoRepository();
        }
        return instance;
    }

    public boolean upsertRawInitialInfo(RawInitialInfo info) {
        return execute(() -> {
            try {
                Dao<RawInitialInfo, Object> dao = getDao(RawInitialInfo.class);
                dao.createOrUpdate(info);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public List<Long> getSavedTeamList() {
        return execute(() -> {
            ArrayList<Long> teamIds = new ArrayList<>();
            try {
                Dao<RawInitialInfo, Object> dao = getDao(RawInitialInfo.class);
                List<RawInitialInfo> teams = dao.queryBuilder().selectColumns("teamId").query();
                for (RawInitialInfo team : teams) {
                    teamIds.add(team.getTeamId());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return teamIds;
        });
    }

    public RawInitialInfo getRawInitialInfo(long teamId) {
        return execute(() -> {
            try {
                Dao<RawInitialInfo, Long> dao = getDao(RawInitialInfo.class);
                return dao.queryForId(teamId);
            } catch (SQLException e) {
                return null;
            }
        });
    }

    public boolean hasInitialInfo(long teamId) {
        return execute(() -> {
            try {
                Dao<RawInitialInfo, Long> dao = getDao(RawInitialInfo.class);
                return dao.queryBuilder()
                        .where()
                        .eq("teamId", teamId)
                        .countOf() > 0;
            } catch (SQLException e) {
                return false;
            }
        });
    }

    public boolean removeInitialInfo(long teamId) {
        return execute(() -> {

            try {
                Dao<RawInitialInfo, Long> dao = getDao(RawInitialInfo.class);
                return dao.deleteById(teamId) > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

    public boolean clear() {
        return execute(() -> {
            try {
                Dao<RawInitialInfo, Long> dao = getDao(RawInitialInfo.class);
                return dao.deleteBuilder()
                        .delete() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        });
    }

}
