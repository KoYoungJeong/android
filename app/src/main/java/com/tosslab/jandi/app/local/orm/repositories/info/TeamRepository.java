package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.start.Team;

import java.sql.SQLException;

public class TeamRepository extends LockExecutorTemplate {
    private static TeamRepository instance;

    synchronized public static TeamRepository getInstance() {
        if (instance == null) {
            instance = new TeamRepository();
        }
        return instance;
    }

    public Team getTeam(long teamId) {
        return execute(() -> {
            try {
                Dao<Team, Long> dao = getHelper().getDao(Team.class);
                return dao.queryForId(teamId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
