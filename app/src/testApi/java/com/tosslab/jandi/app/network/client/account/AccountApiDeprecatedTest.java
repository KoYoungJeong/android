package com.tosslab.jandi.app.network.client.account;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountApiDeprecatedTest {

    private AccountApi.Api api;

    @BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(AccountApi.Api.class);

    }

    @Test
    public void getAccountInfo() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getAccountInfo().execute())).isFalse();
    }

    @Test
    public void updatePrimaryEmail() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.updatePrimaryEmail(new ReqUpdatePrimaryEmailInfo("djdskjd")).execute())).isFalse();
    }

    @Test
    public void updateName() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.updateName(new ReqProfileName("aslkdj")).execute())).isFalse();
    }



}