package com.tosslab.jandi.app.local.orm.repositories.info;

import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.local.orm.repositories.template.LockExecutorTemplate;
import com.tosslab.jandi.app.network.models.start.Bot;
import com.tosslab.jandi.app.network.models.start.InitialInfo;

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

    public boolean addBot(Bot bot) {
        return execute(() -> {
            try {
                Dao<Bot, Long> dao = getHelper().getDao(Bot.class);

                InitialInfo initialInfo = new InitialInfo();
                initialInfo.setTeamId(bot.getTeamId());
                bot.setInitialInfo(initialInfo);

                return dao.create(bot) > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });

    }

    public boolean updateBotStatus(long botId, String status) {
        return execute(() -> {
            try {
                Dao<Bot, Long> dao = getHelper().getDao(Bot.class);
                Bot bot = dao.queryForId(botId);
                bot.setStatus(status);
                return dao.update(bot) > 0;

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });

    }

    public boolean updateBot(Bot bot) {
        return execute(() -> {
            try {
                Dao<Bot, Long> dao = getHelper().getDao(Bot.class);
                Bot savedBot = dao.queryForId(bot.getId());
                bot.setInitialInfo(savedBot.getInitialInfo());
                return dao.update(bot) > 0;

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        });

    }
}
