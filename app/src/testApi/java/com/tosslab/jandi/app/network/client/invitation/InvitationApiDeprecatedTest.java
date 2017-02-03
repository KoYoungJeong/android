package com.tosslab.jandi.app.network.client.invitation;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InvitationApiDeprecatedTest {

    private InvitationApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(InvitationApi.Api.class);
    }

    @Test
    public void acceptOrDeclineInvitation() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.acceptOrDeclineInvitation("", new ReqInvitationAcceptOrIgnore("")))).isFalse();
    }

    @Test
    public void getPedingTeamInfo() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getPedingTeamInfo())).isFalse();
    }


}