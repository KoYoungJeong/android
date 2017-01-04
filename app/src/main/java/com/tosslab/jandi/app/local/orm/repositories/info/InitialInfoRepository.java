package com.tosslab.jandi.app.local.orm.repositories.info;

import com.tosslab.jandi.app.local.orm.repositories.realm.RealmRepository;
import com.tosslab.jandi.app.network.models.start.InitialInfo;

import io.realm.RealmResults;

public class InitialInfoRepository extends RealmRepository {
    private static InitialInfoRepository instance;

    synchronized public static InitialInfoRepository getInstance() {
        if (instance == null) {
            instance = new InitialInfoRepository();
        }
        return instance;
    }

    public boolean upsertInitialInfo(InitialInfo initialInfo) {
        return execute(realm -> {

            realm.executeTransaction(realm1 -> realm.copyToRealmOrUpdate(initialInfo));
            return true;
        });

    }

    public InitialInfo getInitialInfo(long teamId) {
        return execute(realm -> {
            InitialInfo initialInfo = realm.where(InitialInfo.class)
                    .equalTo("teamId", teamId)
                    .findFirst();

            if (initialInfo != null) {
                return realm.copyFromRealm(initialInfo);
            } else {
                return null;
            }
        });
    }

    public boolean hasInitialInfo(long teamId) {
        return execute(realm -> realm.where(InitialInfo.class)
                .equalTo("teamId", teamId)
                .count() > 0);
    }

    public boolean removeInitialInfo(long teamId) {
        return execute(realm -> {
            RealmResults<InitialInfo> teamInfos = realm.where(InitialInfo.class)
                    .equalTo("teamId", teamId)
                    .findAll();
            if (!teamInfos.isEmpty()) {
                realm.executeTransaction(realm1 -> teamInfos.deleteAllFromRealm());
                return true;
            }
            return false;
        });
    }

    public boolean clear() {
        return execute(realm -> {
            realm.executeTransaction(realm1 -> realm.delete(InitialInfo.class));
            return true;
        });
    }

    public long findMyIdFromChats(long roomId) {
        return execute(realm -> {

            InitialInfo info = realm.where(InitialInfo.class).equalTo("chats.id", roomId).findFirst();
            if (info != null && info.getSelf() != null) {
                return info.getSelf().getId();
            }

            return -1L;
        });
    }

    public long findMyIdFromTopics(long roomId) {
        return execute(realm -> {

            InitialInfo info = realm.where(InitialInfo.class).equalTo("topics.id", roomId).findFirst();
            if (info != null && info.getSelf() != null) {
                return info.getSelf().getId();
            }

            return -1L;
        });

    }
}
