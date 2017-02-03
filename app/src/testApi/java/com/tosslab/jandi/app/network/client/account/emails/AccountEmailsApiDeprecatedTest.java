package com.tosslab.jandi.app.network.client.account.emails;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountEmailsApiDeprecatedTest {

    private AccountEmailsApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(AccountEmailsApi.Api.class);
    }

    @Test
    public void requestAddEmail() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.requestAddEmail(new ReqAccountEmail("steve@tosslab.com")))).isFalse();
    }

    @Test
    public void deleteEmail() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.deleteEmail(new ReqAccountEmail("steve@tosslab.com")))).isFalse();
    }


}