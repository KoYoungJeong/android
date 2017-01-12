package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.v4.util.LongSparseArray;

import com.tosslab.jandi.app.local.orm.repositories.template.LockTemplate;
import com.tosslab.jandi.app.network.models.start.TeamPlan;

public class TeamPlanRepository extends LockTemplate {
    private static LongSparseArray<TeamPlanRepository> instance;

    private TeamPlan teamPlan;

    private TeamPlanRepository() {
        super();
    }

    synchronized public static TeamPlanRepository getInstance(long teamId) {
        if (instance == null) {
            instance = new LongSparseArray<>();
        }

        if (instance.indexOfKey(teamId) >= 0) {
            return instance.get(teamId);
        } else {
            TeamPlanRepository value = new TeamPlanRepository();
            instance.put(teamId, value);
            return value;

        }
    }

    public TeamPlan getTeamPlan() {
        return execute(() -> teamPlan);
    }

    public boolean updateTeamPlan(TeamPlan teamPlan) {
        return execute(() -> {
            this.teamPlan = teamPlan;
            return true;
        });
    }
}
