package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.start.InitialInfo;

import java.sql.SQLException;

/**
 * Created by tee on 2016. 8. 17..
 */

public class InitialPollInfoRepository extends LockExecutorTemplate {

    private static InitialPollInfoRepository instance;

    synchronized public static InitialPollInfoRepository getInstance() {
        if (instance == null) {
            instance = new InitialPollInfoRepository();
        }
        return instance;
    }

    public int getVotableCount() {
        return execute(() -> {
            try {
                Dao<InitialInfo.Poll, Object> dao = getDao(InitialInfo.Poll.class);
                return dao.queryBuilder()
                        .queryForFirst().getVotableCount();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    public boolean increaseVotableCount() {
        return execute(() -> {
            try {
                Dao<InitialInfo.Poll, Long> dao = getDao(InitialInfo.Poll.class);
                UpdateBuilder<InitialInfo.Poll, Long> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnExpression("votableCount", "votableCount + 1");
                return updateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean decreaseVotableCount() {
        return execute(() -> {
            try {
                Dao<InitialInfo.Poll, Long> dao = getDao(InitialInfo.Poll.class);
                UpdateBuilder<InitialInfo.Poll, Long> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnExpression("votableCount", "votableCount - 1");
                return updateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }

    public boolean updateVotableCount(int votableCount) {
        return execute(() -> {
            try {
                Dao<InitialInfo.Poll, Object> dao = getDao(InitialInfo.Poll.class);
                UpdateBuilder<InitialInfo.Poll, Object> updateBuilder = dao.updateBuilder();
                updateBuilder.updateColumnValue("votableCount", votableCount);
                return updateBuilder.update() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });
    }
}