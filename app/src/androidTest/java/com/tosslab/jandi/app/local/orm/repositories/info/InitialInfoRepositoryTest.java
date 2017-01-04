package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Before;
import org.junit.Test;

import io.realm.Realm;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;


@org.junit.runner.RunWith(AndroidJUnit4.class)
public class InitialInfoRepositoryTest {
    private static InitialInfo initializeInfo;
    private static long teamId;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
        teamId = TeamInfoLoader.getInstance().getTeamId();
        initializeInfo = new StartApi(RetrofitBuilder.getInstance()).getInitializeInfo(teamId);
    }

    @Test
    public void findMyIdFromChats() throws Exception {
        long id = initializeInfo.getChats().get(0).getId();
        assertThat(InitialInfoRepository.getInstance().findMyIdFromChats(id)).isEqualTo(initializeInfo.getSelf().getId());
        assertThat(InitialInfoRepository.getInstance().findMyIdFromChats(1L)).isEqualTo(-1L);

    }

    @Test
    public void findMyIdFromTopics() throws Exception {
        long id = initializeInfo.getTopics().get(0).getId();
        assertThat(InitialInfoRepository.getInstance().findMyIdFromTopics(id)).isEqualTo(initializeInfo.getSelf().getId());
        assertThat(InitialInfoRepository.getInstance().findMyIdFromTopics(1L)).isEqualTo(-1L);

    }

    @Before
    public void setUp() throws Exception {
        Realm.getDefaultInstance().executeTransaction(realm -> realm.deleteAll());
        InitialInfoRepository.getInstance().upsertInitialInfo(initializeInfo);
        TeamInfoLoader.getInstance().refresh();
    }

    @Test
    public void testGetInitialInfo() throws Exception {
        InitialInfo initialInfo = InitialInfoRepository.getInstance().getInitialInfo(teamId);
        assertThat(initialInfo).isNotNull();
        assertThat(initialInfo.getSelf().getId()).isEqualTo(initializeInfo.getSelf().getId());
    }

    @Test
    public void testHasInitialInfo() throws Exception {
        assertThat(InitialInfoRepository.getInstance().hasInitialInfo(teamId)).isTrue();

        Realm.getDefaultInstance().executeTransaction(realm -> realm.deleteAll());
        assertThat(InitialInfoRepository.getInstance().hasInitialInfo(teamId)).isFalse();

    }

    @Test
    public void testRemoveInitialInfo() throws Exception {
        assertThat(InitialInfoRepository.getInstance().removeInitialInfo(teamId)).isTrue();
        assertThat(InitialInfoRepository.getInstance().getInitialInfo(teamId)).isNull();
    }

    @Test
    public void testClear() throws Exception {
        InitialInfoRepository.getInstance().clear();
        assertThat(Realm.getDefaultInstance().where(InitialInfo.class).count()).isEqualTo(0);
        assertThat(InitialInfoRepository.getInstance().getInitialInfo(teamId)).isNull();
    }

    @Test
    public void testUpsertInitialInfo() throws Exception {
        Realm.getDefaultInstance().executeTransaction(realm -> realm.deleteAll());
        boolean success = InitialInfoRepository.getInstance().upsertInitialInfo(initializeInfo);
        assertThat(success).isTrue();

        InitialInfo initialInfo = InitialInfoRepository.getInstance().getInitialInfo(teamId);

        assertThat(initialInfo).isNotNull();
        assertThat(initialInfo.getTeam()).isNotNull();
        assertThat(initialInfo.getTopics()).isNotNull().isNotEmpty();
        assertThat(initialInfo.getChats()).isNotNull().isNotEmpty();
        assertThat(initialInfo.getFolders()).isNotNull().isNotEmpty();
        assertThat(initialInfo.getMembers()).isNotNull().isNotEmpty();
        assertThat(initialInfo.getBots()).isNotNull();

    }
}