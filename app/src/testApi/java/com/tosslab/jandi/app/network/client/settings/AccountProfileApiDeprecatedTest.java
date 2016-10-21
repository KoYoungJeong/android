package com.tosslab.jandi.app.network.client.settings;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqProfileName;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountProfileApiDeprecatedTest {

    private AccountProfileApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(AccountProfileApi.Api.class);
    }

    @Test
    public void changeName() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.changeName(new ReqProfileName("aslkdj")).execute())).isFalse();
    }

    @Test
    public void changePrimaryEmail() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.changePrimaryEmail(new ReqAccountEmail("aslkdj")).execute())).isFalse();
    }


}