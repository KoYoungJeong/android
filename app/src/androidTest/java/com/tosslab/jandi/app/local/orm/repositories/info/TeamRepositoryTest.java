package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.start.Team;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Before;
import org.junit.Test;

import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;


@org.junit.runner.RunWith(AndroidJUnit4.class)
public class TeamRepositoryTest {

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
        InitialInfoRepository.getInstance().upsertInitialInfo(initializeInfo);
        TeamInfoLoader.getInstance().refresh();
    }

    @Test
    public void testGetTeam() throws Exception {
        Team team = TeamRepository.getInstance().getTeam(teamId);
        assertThat(team).isNotNull();
        assertThat(team.getId()).isEqualTo(teamId);
    }

    @Test
    public void testUpdateTeam() throws Exception {
        Team team = initializeInfo.getTeam();
        String name = "hello world";
        String domain = "hello";
        team.setName(name);
        team.setDomain(domain);
        assertThat(TeamRepository.getInstance().updateTeam(team)).isTrue();

        Team newTeam = TeamRepository.getInstance().getTeam(teamId);
        assertThat(newTeam.getName()).isEqualToIgnoringCase(name);
        assertThat(newTeam.getDomain()).isEqualToIgnoringCase(domain);

    }
}