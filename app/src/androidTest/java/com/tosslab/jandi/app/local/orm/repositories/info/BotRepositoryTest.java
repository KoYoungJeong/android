package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.start.Bot;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.realm.Realm;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;


@org.junit.runner.RunWith(AndroidJUnit4.class)
public class BotRepositoryTest {

    private static long teamId;
    private static InitialInfo initializeInfo;

    @BeforeClass
    public static void setUpClass() throws RetrofitException {
        BaseInitUtil.initData();
        teamId = TeamInfoLoader.getInstance().getTeamId();
        initializeInfo = new StartApi(RetrofitBuilder.getInstance()).getInitializeInfo(teamId);
    }

    @Before
    public void setUp() throws Exception {
        Realm.getDefaultInstance().executeTransaction(realm -> realm.deleteAll());
        InitialInfoRepository.getInstance().upsertInitialInfo(initializeInfo);
        TeamInfoLoader.getInstance().refresh();
    }

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
        bot.setStatus("enabled");
        bot.setPhotoUrl("http");
        bot.setType("bot");
        BotRepository.getInstance().addBot(bot);
        return bot.getId();
    }

    @Test
    public void testAddBot() throws Exception {
        int botId = 1;

        Bot bot = new Bot();
        bot.setId(botId);
        bot.setName("hello world");
        bot.setTeamId(1);
        bot.setType("connect");

        BotRepository.getInstance().addBot(bot);

        Bot bot1 = BotRepository.getInstance().getBot(botId);

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

        Bot bot = new Bot();
        bot.setName(name);
        bot.setId(botId);
        bot.setTeamId(1);
        bot.setStatus("enabled");
        bot.setPhotoUrl("http");
        bot.setType("bot");
        BotRepository.getInstance().updateBot(bot);

        bot = BotRepository.getInstance().getBot(botId);

        assertThat(bot.getName()).isEqualToIgnoringCase(name);
    }
}