package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
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

    public boolean updateTeamName(long teamId, String name) {
        return execute(() -> {
            try {
                Dao<Team, Long> dao = getDao(Team.class);
                UpdateBuilder<Team, Long> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnValue("name", name)
                        .where()
                        .eq("id", teamId);
                return updateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean updateTeamDomain(long teamId, String domain) {
        return execute(() -> {
            try {
                Dao<Team, Long> dao = getDao(Team.class);
                UpdateBuilder<Team, Long> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnValue("domain", domain)
                        .where()
                        .eq("id", teamId);
                return updateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean updateTeam(Team team) {
        return execute(() -> {
            try {
                Dao<Team, Object> dao = getDao(Team.class);
                dao.createOrUpdate(team);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }
}
