package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.start.RawInitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Before;
import org.junit.Test;

import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;

@org.junit.runner.RunWith(AndroidJUnit4.class)
public class InitialPollInfoRepositoryTest {

    private static final int STANDARD_VALUE = 10;

    private static String initializeInfo;
    private static long teamId;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
        teamId = TeamInfoLoader.getInstance().getTeamId();
        initializeInfo = new StartApi(RetrofitBuilder.getInstance()).getRawInitializeInfo(teamId);
    }

    @Before
    public void setUp() throws Exception {
        InitialInfoRepository.getInstance().upsertRawInitialInfo(new RawInitialInfo(teamId, initializeInfo));
        TeamInfoLoader.getInstance().refresh();

        InitialPollInfoRepository.getInstance().updateVotableCount(STANDARD_VALUE);
    }

    @Test
    public void plusVotableCount() throws Exception {
        assertThat(InitialPollInfoRepository.getInstance().increaseVotableCount()).isTrue();

        assertThat(InitialPollInfoRepository.getInstance().getVotableCount()).isEqualTo(STANDARD_VALUE + 1);
    }

    @Test
    public void minusVotableCount() throws Exception {
        assertThat(InitialPollInfoRepository.getInstance().decreaseVotableCount()).isTrue();
        assertThat(InitialPollInfoRepository.getInstance().getVotableCount()).isEqualTo(STANDARD_VALUE - 1);
    }

    @Test
    public void getVotableCount() throws Exception {
        assertThat(InitialPollInfoRepository.getInstance().getVotableCount())
                .isEqualTo(STANDARD_VALUE);
    }

    @Test
    public void updateVotableCount() throws Exception {
        assertThat(InitialPollInfoRepository.getInstance().updateVotableCount(STANDARD_VALUE + 5)).isTrue();
        assertThat(InitialPollInfoRepository.getInstance().getVotableCount()).isEqualTo(STANDARD_VALUE + 5);
    }


}


