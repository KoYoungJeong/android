package com.tosslab.jandi.app.local.orm.repositories.info;

import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.start.Mention;
import com.tosslab.jandi.app.network.models.start.RawInitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Before;
import org.junit.Test;

import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class InitialMentionInfoRepositoryTest {

    private static String initializeInfo;
    private static long teamId;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
        teamId = TeamInfoLoader.getInstance().getTeamId();
        initializeInfo = new StartApi(InnerApiRetrofitBuilder.getInstance()).getRawInitializeInfo(teamId);
    }

    @Before
    public void setUp() throws Exception {
        InitialInfoRepository.getInstance().clear();
        InitialInfoRepository.getInstance().upsertRawInitialInfo(new RawInitialInfo(teamId, initializeInfo));
        TeamInfoLoader.getInstance().refresh();
    }

    @Test
    public void getMention() throws Exception {
        Mention mention = InitialMentionInfoRepository.getInstance().getMention();
        assertThat(mention).isNotNull();
        assertThat(mention.getLastMentionedMessageId()).isEqualTo(TeamInfoLoader.getInstance().getMention().getLastMentionedMessageId());
        assertThat(mention.getUnreadCount()).isEqualTo(TeamInfoLoader.getInstance().getMention().getUnreadCount());
    }

    @Test
    public void upsertMention() throws Exception {
        Mention mention = new Mention();
        mention.setUnreadCount(12);
        mention.setLastMentionedMessageId(121);
        InitialMentionInfoRepository.getInstance().upsertMention(mention);

        {
            Mention mention1 = InitialMentionInfoRepository.getInstance().getMention();
            assertThat(mention1.getUnreadCount()).isEqualTo(mention.getUnreadCount());
            assertThat(mention1.getLastMentionedMessageId()).isEqualTo(mention.getLastMentionedMessageId());
        }

        {
            InitialMentionInfoRepository.getInstance().clearUnreadCount();

            Mention mention1 = InitialMentionInfoRepository.getInstance().getMention();
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
        int old = InitialMentionInfoRepository.getInstance().getMention().getUnreadCount();
        assertThat(InitialMentionInfoRepository.getInstance().increaseUnreadCount()).isTrue();
        assertThat(InitialMentionInfoRepository.getInstance().getMention().getUnreadCount()).isGreaterThan(old);
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