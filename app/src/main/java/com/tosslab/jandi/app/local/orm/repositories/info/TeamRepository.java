package com.tosslab.jandi.app.local.orm.repositories.info;

import com.tosslab.jandi.app.local.orm.repositories.realm.RealmRepository;
import com.tosslab.jandi.app.network.models.start.Team;

public class TeamRepository extends RealmRepository {
    private static TeamRepository instance;

    synchronized public static TeamRepository getInstance() {
        if (instance == null) {
            instance = new TeamRepository();
        }
        return instance;
    }

    public Team getTeam(long teamId) {
        return execute(realm -> {
            Team it = realm.where(Team.class)
                    .equalTo("id", teamId)
                    .findFirst();
            if (it != null) {
                return realm.copyFromRealm(it);
            } else {
                return null;
            }
        });
    }

    public boolean updateTeam(Team team) {
        return execute(realm -> {

            realm.executeTransaction(realm1 -> realm.insertOrUpdate(team));
            return true;
        });
    }
}
