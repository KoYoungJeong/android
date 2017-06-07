package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.start.RawInitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;


@org.junit.runner.RunWith(AndroidJUnit4.class)
public class InitialInfoRepositoryTest {
    private static String initializeInfo;
    private static long teamId;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
        teamId = TeamInfoLoader.getInstance().getTeamId();
        initializeInfo = new StartApi(InnerApiRetrofitBuilder.getInstance()).getRawInitializeInfo(teamId);
    }

    @Test
    public void getSavedTeamList() throws Exception {
        List<Long> savedTeamList = InitialInfoRepository.getInstance().getSavedTeamList();
        assertThat(savedTeamList).contains(teamId);
    }

    @Before
    public void setUp() throws Exception {
        InitialInfoRepository.getInstance().upsertRawInitialInfo(new RawInitialInfo(teamId, initializeInfo));
        TeamInfoLoader.getInstance().refresh();
    }

    @Test
    public void testGetInitialInfo() throws Exception {
        RawInitialInfo initialInfo = InitialInfoRepository.getInstance().getRawInitialInfo(teamId);
        assertThat(initialInfo).isNotNull();
    }

    @Test
    public void testHasInitialInfo() throws Exception {
        assertThat(InitialInfoRepository.getInstance().hasInitialInfo(teamId)).isTrue();

        InitialInfoRepository.getInstance().clear();
        assertThat(InitialInfoRepository.getInstance().hasInitialInfo(teamId)).isFalse();

    }

    @Test
    public void testRemoveInitialInfo() throws Exception {
        assertThat(InitialInfoRepository.getInstance().removeInitialInfo(teamId)).isTrue();
        assertThat(InitialInfoRepository.getInstance().getRawInitialInfo(teamId)).isNull();
    }

    @Test
    public void testClear() throws Exception {
        InitialInfoRepository.getInstance().clear();
        assertThat(InitialInfoRepository.getInstance().getRawInitialInfo(teamId)).isNull();
    }

    @Test
    public void testUpsertInitialInfo() throws Exception {
        InitialInfoRepository.getInstance().clear();
        boolean success = InitialInfoRepository.getInstance().upsertRawInitialInfo(new RawInitialInfo(teamId, initializeInfo));
        assertThat(success).isTrue();

        RawInitialInfo initialInfo = InitialInfoRepository.getInstance().getRawInitialInfo(teamId);


        assertThat(initialInfo).isNotNull();
        assertThat(initialInfo.getTeamId()).isEqualTo(teamId);

    }
}