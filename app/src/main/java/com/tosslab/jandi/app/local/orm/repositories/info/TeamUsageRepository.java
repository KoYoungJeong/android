package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.v4.util.LongSparseArray;

import com.tosslab.jandi.app.local.orm.repositories.template.LockTemplate;
import com.tosslab.jandi.app.network.models.start.TeamUsage;

/**
 * Created by tee on 2017. 3. 2..
 */

public class TeamUsageRepository extends LockTemplate {
    private static LongSparseArray<TeamUsageRepository> instance;

    private TeamUsage teamUsage;

    private TeamUsageRepository() {
        super();
    }

    synchronized public static TeamUsageRepository getInstance(long teamId) {
        if (instance == null) {
            instance = new LongSparseArray<>();
        }

        if (instance.indexOfKey(teamId) >= 0) {
            return instance.get(teamId);
        } else {
            TeamUsageRepository value = new TeamUsageRepository();
            instance.put(teamId, value);
            return value;
        }
    }

    public TeamUsage getTeamUsage() {
        return execute(() -> teamUsage);
    }

    public boolean updateTeamUsage(TeamUsage teamUsage) {
        return execute(() -> {
            this.teamUsage = teamUsage;
            return true;
        });
    }
}
