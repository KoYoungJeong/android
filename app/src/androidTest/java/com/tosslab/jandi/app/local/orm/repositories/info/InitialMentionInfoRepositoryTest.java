package com.tosslab.jandi.app.local.orm.repositories.info;

import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.start.Mention;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Before;
import org.junit.Test;

import io.realm.Realm;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class InitialMentionInfoRepositoryTest {

    private static InitialInfo initializeInfo;
    private static long teamId;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
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
    public void getMention() throws Exception {
        Mention mention = InitialMentionInfoRepository.getInstance().getMention();
        assertThat(mention).isNotNull();
        assertThat(mention.getLastMentionedMessageId()).isEqualTo(initializeInfo.getMention().getLastMentionedMessageId());
        assertThat(mention.getUnreadCount()).isEqualTo(initializeInfo.getMention().getUnreadCount());
    }

    @Test
    public void upsertMention() throws Exception {
        Mention mention = new Mention();
        mention.setId(teamId);
        mention.setUnreadCount(12);
        mention.setLastMentionedMessageId(121);
        InitialMentionInfoRepository.getInstance().upsertMention(mention);

        {
            Mention mention1 = InitialMentionInfoRepository.getInstance().getMention();
            assertThat(mention1.getId()).isEqualTo(teamId);
            assertThat(mention1.getUnreadCount()).isEqualTo(mention.getUnreadCount());
            assertThat(mention1.getLastMentionedMessageId()).isEqualTo(mention.getLastMentionedMessageId());
        }

        {
            Realm.getDefaultInstance().executeTransaction(realm -> realm.delete(Mention.class));

            Mention mention1 = InitialMentionInfoRepository.getInstance().getMention();
            assertThat(mention1.getId()).isEqualTo(teamId);
            assertThat(mention1.getUnreadCount()).isEqualTo(0);
            assertThat(mention1.getLastMentionedMessageId()).isEqualTo(-1L);
        }

    }

    @Test
    public void clearUnreadCount() throws Exception {
        assertThat(InitialMentionInfoRepository.getInstance().clearUnreadCount()).isTrue();
        assertThat(InitialMentionInfoRepository.getInstance().getMention().getUnreadCount()).isEqualTo(0);
    }

    @Test
    public void increaseUnreadCount() throws Exception {
        assertThat(InitialMentionInfoRepository.getInstance().increaseUnreadCount()).isTrue();
        assertThat(InitialMentionInfoRepository.getInstance().getMention().getUnreadCount()).isGreaterThan(initializeInfo.getMention().getUnreadCount());
    }

    @Test
    public void decreaseUnreadCount() throws Exception {
        InitialMentionInfoRepository.getInstance().increaseUnreadCount();
        InitialMentionInfoRepository.getInstance().increaseUnreadCount();

        int unreadCount = InitialMentionInfoRepository.getInstance().getMention().getUnreadCount();

        assertThat(InitialMentionInfoRepository.getInstance().decreaseUnreadCount()).isTrue();
        assertThat(InitialMentionInfoRepository.getInstance().getMention().getUnreadCount()).isLessThan(unreadCount);
    }


}