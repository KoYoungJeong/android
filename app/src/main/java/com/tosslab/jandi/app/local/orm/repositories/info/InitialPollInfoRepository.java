package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
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
                Dao<InitialInfo.Poll, ?> dao = InitialPollInfoRepository
                        .this.getHelper().getDao(InitialInfo.Poll.class);
                return dao.queryBuilder()
                        .query().get(0).getVotableCount();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }

    public void plusVotableCount() {
        execute(new Executor<Void>() {
            @Override
            public Void execute() {
                try {
                    Dao<InitialInfo.Poll, ?> dao = getHelper().getDao(InitialInfo.Poll.class);
                    InitialInfo.Poll poll = new InitialInfo.Poll();
                    poll.setVotableCount(getVotableCount() + 1);
                    dao.update(poll);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    public void minusVotableCount() {
        execute(new Executor<Void>() {
            @Override
            public Void execute() {
                try {
                    Dao<InitialInfo.Poll, ?> dao = getHelper().getDao(InitialInfo.Poll.class);
                    InitialInfo.Poll poll = new InitialInfo.Poll();
                    poll.setVotableCount(getVotableCount() - 1);
                    dao.update(poll);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

}