package com.tosslab.jandi.app.network.client.account.password;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountPasswordApiDeprecatedTest {

    private AccountPasswordApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(AccountPasswordApi.Api.class);

    }

    @Test
    public void resetPassword() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.resetPassword(new ReqAccountEmail(OkHttpClientTestFactory.USERID)).execute())).isFalse();
    }

}