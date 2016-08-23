package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.start.InitialInfo;

import java.sql.SQLException;

public class SelfRepository extends LockExecutorTemplate {
    private static SelfRepository instance;

    synchronized public static SelfRepository getInstance() {
        if (instance == null) {
            instance = new SelfRepository();
        }
        return instance;
    }

    public boolean isMe(long userId) {
        return execute(() -> {
            try {
                Dao<InitialInfo.Self, ?> dao = SelfRepository.this.getHelper().getDao(InitialInfo.Self.class);
                return dao.queryBuilder()
                        .where()
                        .eq("id", userId)
                        .countOf() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }
}
