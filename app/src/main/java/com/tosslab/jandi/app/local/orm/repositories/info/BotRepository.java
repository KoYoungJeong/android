package com.tosslab.jandi.app.local.orm.repositories.info;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.realm.RealmRepository;
import com.tosslab.jandi.app.network.models.start.Bot;

public class BotRepository extends RealmRepository {
    private static BotRepository instance;

    synchronized public static BotRepository getInstance() {
        if (instance == null) {
            instance = new BotRepository();
        }
        return instance;
    }

    public Bot getBot(long memberId) {
        return execute(realm -> realm.where(Bot.class)
                .equalTo("id", memberId)
                .findFirst());
    }

    public boolean addBot(Bot bot) {
        return execute(realm -> {

            if (realm.where(Bot.class).equalTo("id", bot.getId()).count() <= 0) {
                bot.setTeamId(AccountRepository.getRepository().getSelectedTeamId());
                realm.executeTransaction(it -> realm.copyToRealmOrUpdate(bot));
            }


            return true;
        });

    }

    public boolean updateBotStatus(long botId, String status) {
        return execute(realm -> {

            Bot bot = realm.where(Bot.class).equalTo("id", botId).findFirst();
            if (bot != null) {
                realm.executeTransaction(realm1 -> bot.setStatus(status));
                return true;
            }

            return false;
        });

    }

    public boolean updateBot(Bot bot) {
        return execute(realm -> {

            bot.setTeamId(AccountRepository.getRepository().getSelectedTeamId());
            realm.executeTransaction(it -> realm.copyToRealmOrUpdate(bot));

            return true;
        });

    }
}
