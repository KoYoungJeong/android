package com.tosslab.jandi.app.local.orm.repositories.info;


import android.support.annotation.VisibleForTesting;
import android.support.v4.util.LongSparseArray;

import com.tosslab.jandi.app.local.orm.repositories.template.LockTemplate;
import com.tosslab.jandi.app.network.models.start.Bot;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.WebhookBot;

public class BotRepository extends LockTemplate {
    private static LongSparseArray<BotRepository> instance;

    private LongSparseArray<WebhookBot> bots;

    private BotRepository() {
        super();
        bots = new LongSparseArray<>();
    }

    synchronized public static BotRepository getInstance(long teamId) {
        if (instance == null) {
            instance = new LongSparseArray<>();
        }

        if (instance.indexOfKey(teamId) >= 0) {
            return instance.get(teamId);
        } else {
            BotRepository value = new BotRepository();
            instance.put(teamId, value);
            return value;

        }
    }

    synchronized public static BotRepository getInstance() {
        return getInstance(TeamInfoLoader.getInstance().getTeamId());
    }

    @VisibleForTesting
    public Bot getBot(long memberId) {
        return execute(() -> {
            if (hasBot(memberId)) {
                return bots.get(memberId).getRaw();
            } else {
                return null;
            }
        });
    }

    public boolean addBot(Bot bot) {
        return execute(() -> {

            if (!hasBot(bot.getId())) {
                bots.put(bot.getId(), new WebhookBot(bot));
                return true;
            } else {
                return false;
            }
        });

    }

    public boolean updateBotStatus(long botId, String status) {
        return execute(() -> {
            if (hasBot(botId)) {
                bots.get(botId).getRaw().setStatus(status);
                return true;
            } else {
                return false;
            }
        });

    }

    public boolean updateBot(Bot bot) {
        return execute(() -> {

            if (hasBot(bot.getId())) {
                Bot saved = bots.get(bot.getId()).getRaw();
                saved.setType(bot.getType());
                saved.setBotType(bot.getBotType());
                saved.setStatus(bot.getStatus());
                saved.setName(bot.getName());
                saved.setPhotoUrl(bot.getPhotoUrl());
            } else {
                bots.put(bot.getId(), new WebhookBot(bot));
            }

            return true;
        });

    }

    public boolean hasBot(long id) {
        return execute(() -> bots.indexOfKey(id) >= 0);
    }


    public WebhookBot getWebhookBot(long botId) {
        return execute(() -> bots.get(botId));
    }

    public void addWebhookBot(long botId, WebhookBot bot) {
        execute(() -> {
            bots.put(botId, bot);
            return true;
        });
    }

    public void clear() {
        execute(() -> {
            bots.clear();
            return true;
        });
    }
}
