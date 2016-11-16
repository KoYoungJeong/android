package com.tosslab.jandi.app.local.orm.repositories.info;

import com.tosslab.jandi.app.local.orm.repositories.realm.RealmRepository;
import com.tosslab.jandi.app.network.models.start.Self;

public class SelfRepository extends RealmRepository {
    private static SelfRepository instance;

    synchronized public static SelfRepository getInstance() {
        if (instance == null) {
            instance = new SelfRepository();
        }
        return instance;
    }

    public boolean isMe(long userId) {
        return execute((realm) -> realm.where(Self.class)
                .equalTo("id", userId)
                .count() > 0);
    }
}
