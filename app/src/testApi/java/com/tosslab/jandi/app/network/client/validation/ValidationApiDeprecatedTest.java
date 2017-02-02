package com.tosslab.jandi.app.network.client.validation;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidationApiDeprecatedTest {

    private ValidationApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(ValidationApi.Api.class);
    }

    @Test
    public void validDomain() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.validDomain("asd").execute())).isFalse();
    }

    @Test
    public void getEmailTypo() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getEmailTypo().execute())).isFalse();

    }
}