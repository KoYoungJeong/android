package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.team.rank.Rank;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RankRepository extends LockExecutorTemplate {
    private static RankRepository instance;

    private RankRepository() { }

    synchronized public static RankRepository getInstance() {
        if (instance == null) {
            instance = new RankRepository();
        }
        return instance;
    }

    public boolean addRanks(List<Rank> ranks) {
        return execute(() -> {

            try {
                Dao<Rank, Object> dao = getDao(Rank.class);
                dao.callBatchTasks(() -> {
                    for (Rank rank : ranks) {
                        dao.createOrUpdate(rank);
                    }
                    return true;
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        });
    }

    public boolean hasRanks(long teamId) {
        return execute(() -> {

            try {
                return getDao(Rank.class)
                        .queryBuilder()
                        .where()
                        .eq("teamId", teamId)
                        .countOf() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }


            return false;
        });
    }

    public List<Rank> getRanks(long teamId) {
        return execute(() -> {
            try {
                Dao<Rank, Object> dao = getDao(Rank.class);
                return dao.queryForEq("teamId", teamId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new ArrayList<Rank>();
        });
    }
}
