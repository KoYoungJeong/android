package com.tosslab.jandi.app.local.orm.repositories.info;

import com.tosslab.jandi.app.local.orm.repositories.realm.RealmRepository;
import com.tosslab.jandi.app.network.models.team.rank.Rank;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;

public class RankRepository extends RealmRepository {
    private static RankRepository instance;

    synchronized public static RankRepository getInstance() {
        if (instance == null) {
            instance = new RankRepository();
        }
        return instance;
    }

    public boolean addRanks(List<Rank> ranks) {
        return execute(realm -> {

            realm.executeTransaction(realm1 -> realm.copyToRealm(ranks));

            return false;
        });
    }

    public boolean hasRanks(long teamId) {
        return execute(realm -> realm.where(Rank.class)
                .equalTo("teamId", teamId)
                .count() > 0);
    }

    public List<Rank> getRanks(long teamId) {
        return execute(realm -> {
            RealmResults<Rank> ranks = realm.where(Rank.class)
                    .equalTo("teamId", teamId)
                    .findAll();
            if (ranks != null && !ranks.isEmpty()) {
                return realm.copyFromRealm(ranks);
            } else {
                return new ArrayList<Rank>();
            }
        });
    }
}
