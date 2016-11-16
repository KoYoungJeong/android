package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.start.Poll;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Before;
import org.junit.Test;

import io.realm.Realm;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by tee on 2016. 8. 17..
 */
@org.junit.runner.RunWith(AndroidJUnit4.class)
public class InitialPollInfoRepositoryTest {

    private static final int STANDARD_VALUE = 10;

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

        Realm.getDefaultInstance().executeTransaction(realm -> {
            realm.where(Poll.class).equalTo("id",teamId).findFirst().setVotableCount(STANDARD_VALUE);
        });
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


