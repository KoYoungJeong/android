package com.tosslab.jandi.app.network.client.teams;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqInvitationMembers;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class TeamApiDeprecatedTest {
    private TeamApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(TeamApi.Api.class);
    }

    @Test
    public void createNewTeam() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.createNewTeam(new ReqCreateNewTeam("tosslab","tosslab")).execute())).isFalse();
    }

    @Test
    public void inviteToTeam() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.inviteToTeam(1, new ReqInvitationMembers(1, new ArrayList<String>(), "asd")).execute())).isFalse();
    }

    @Test
    public void getTeamInfo() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getTeamInfo(1).execute())).isFalse();
    }

    @Test
    public void cancelInviteTeam() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.cancelInviteTeam(1, 1).execute())).isFalse();
    }

    @Test
    public void getRanks() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getRanks(1).execute())).isFalse();
    }


}