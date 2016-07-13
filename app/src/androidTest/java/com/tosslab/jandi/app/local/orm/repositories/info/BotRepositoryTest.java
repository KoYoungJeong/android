package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.models.start.Bot;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


@org.junit.runner.RunWith(AndroidJUnit4.class)
public class BotRepositoryTest {

    @Test
    public void testGetBot() throws Exception {
        long botId = getBotId();
        Bot bot = BotRepository.getInstance().getBot(botId);
        assertThat(bot).isNotNull();
    }

    private long getBotId() {
        Bot bot = new Bot();
        bot.setName("bot");
        bot.setId(System.currentTimeMillis());
        bot.setTeamId(1);
        bot.setStatus("enabled");
        bot.setPhotoUrl("http");
        bot.setType("bot");
        BotRepository.getInstance().addBot(bot);
        return bot.getId();
    }

    @Test
    public void testAddBot() throws Exception {
        Bot bot = new Bot();
        bot.setId(1l);
        bot.setName("hello world");
        bot.setTeamId(1);
        bot.setType("connect");

        BotRepository.getInstance().addBot(bot);

        Bot bot1 = BotRepository.getInstance().getBot(bot.getId());

        assertThat(bot1.getId()).isEqualTo(bot.getId());
        assertThat(bot1.getName()).isEqualTo(bot.getName());
        assertThat(bot1.getTeamId()).isEqualTo(bot.getTeamId());
        assertThat(bot1.getType()).isEqualTo(bot.getType());
    }

    @Test
    public void testUpdateBotStatus() throws Exception {
        long botId = getBotId();
        BotRepository.getInstance().updateBotStatus(botId, "deleted");
        Bot bot = BotRepository.getInstance().getBot(botId);
        assertThat(bot.getStatus()).isEqualToIgnoringCase("deleted");
    }

    @Test
    public void testUpdateBot() throws Exception {
        String name = "hello";

        long botId = getBotId();
        Bot bot = BotRepository.getInstance().getBot(botId);

        bot.setName(name);
        BotRepository.getInstance().updateBot(bot);

        bot = BotRepository.getInstance().getBot(botId);

        assertThat(bot.getName()).isEqualToIgnoringCase(name);
    }
}