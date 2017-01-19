package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.v4.util.LongSparseArray;

import com.tosslab.jandi.app.local.orm.repositories.template.LockTemplate;
import com.tosslab.jandi.app.network.models.start.Team;

public class TeamRepository extends LockTemplate {
    private static LongSparseArray<TeamRepository> instance;

    private Team team;

    private TeamRepository() {
        super();
    }

    synchronized public static TeamRepository getInstance(long teamId) {
        if (instance == null) {
            instance = new LongSparseArray<>();
        }

        if (instance.indexOfKey(teamId) >= 0) {
            return instance.get(teamId);
        } else {
            TeamRepository value = new TeamRepository();
            instance.put(teamId, value);
            return value;

        }
    }

    public Team getTeam() {
        return execute(() -> team);
    }

    public boolean updateTeam(Team team) {
        return execute(() -> {
            this.team = team;
            return true;
        });
    }
}
