package com.tosslab.jandi.app.local.orm.repositories;

import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.start.InitializeInfo;

import java.sql.SQLException;

public class InitialInfoRepository extends LockExecutorTemplate {
    private static InitialInfoRepository instance;

    synchronized public static InitialInfoRepository getInstance() {
        if (instance == null) {
            instance = new InitialInfoRepository();
        }
        return instance;
    }

    public boolean upsertInitialInfo(InitializeInfo initializeInfo) {
        return execute(() -> {
            try {
                Dao<InitializeInfo, ?> dao = getHelper().getDao(InitializeInfo.class);
                dao.createOrUpdate(initializeInfo);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });

    }

    public InitializeInfo getInitialInfo(long teamId) {
        return execute(() -> {
            try {
                Dao<InitializeInfo, ?> dao = getHelper().getDao(InitializeInfo.class);
                return dao.queryBuilder()
                        .where()
                        .eq("teamId", teamId)
                        .queryForFirst();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
