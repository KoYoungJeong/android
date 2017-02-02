package com.tosslab.jandi.app.network.client.member;

import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.member.MemberInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;

import org.junit.Before;
import org.junit.Test;

import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class MemberApiTest {

    private MemberApi memberApi;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();

    }

    @Before
    public void setUp() throws Exception {
        memberApi = new MemberApi(RetrofitBuilder.getInstance());
    }

    @Test
    public void getMemberInfo() throws Exception {
        User user = TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId());
        MemberInfo memberInfo = memberApi.getMemberInfo(TeamInfoLoader.getInstance().getTeamId(), user.getId());

        assertThat(memberInfo.getTeamId()).isEqualTo(TeamInfoLoader.getInstance().getTeamId());
        assertThat(memberInfo.getId()).isEqualTo(user.getId());
        assertThat(memberInfo.getName()).isEqualTo(user.getName());
        assertThat(user.getPhotoUrl()).contains(memberInfo.getPhotoUrl());
        assertThat(memberInfo.getJoinTopics().size()).isGreaterThanOrEqualTo(1);
    }


}