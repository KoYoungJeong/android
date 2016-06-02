package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.start.Bot;

import java.sql.SQLException;

public class BotRepository extends LockExecutorTemplate {
    private static BotRepository instance;

    synchronized public static BotRepository getInstance() {
        if (instance == null) {
            instance = new BotRepository();
        }
        return instance;
    }

    public Bot getBot(long memberId) {
        return execute(() -> {
            try {
                Dao<Bot, Long> dao = getHelper().getDao(Bot.class);
                return dao.queryForId(memberId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
