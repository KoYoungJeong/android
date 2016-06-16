package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.start.Bot;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Before;
import org.junit.Test;

import rx.Observable;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;


@org.junit.runner.RunWith(AndroidJUnit4.class)
public class BotRepositoryTest {

    private static final String TAG = "BotRepositoryTest";
    private static InitialInfo initializeInfo;
    private static Observable<Long> longObservable;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
        initializeInfo = new StartApi(RetrofitBuilder.getInstance()).getInitializeInfo(TeamInfoLoader.getInstance().getTeamId());
        longObservable = Observable.from(initializeInfo.getBots())
                .takeFirst(bot -> bot != null)
                .map(Bot::getId)
                .replay()
                .refCount();

    }

    @Before
    public void setUp() throws Exception {
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
        return longObservable.toBlocking().firstOrDefault(-1L);
    }

    @Test
    public void testAddBot() throws Exception {
        Bot bot = new Bot();
        bot.setId(1l);
        bot.setName("hello world");
        bot.setInitialInfo(initializeInfo);
        bot.setTeamId(initializeInfo.getTeam().getId());
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