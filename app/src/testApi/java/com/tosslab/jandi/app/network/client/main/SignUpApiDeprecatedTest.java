package com.tosslab.jandi.app.network.client.main;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqAccountVerification;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SignUpApiDeprecatedTest {

    private SignUpApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(SignUpApi.Api.class);
    }

    @Test
    public void signUpAccount() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.signUpAccount(new ReqSignUpInfo("", "", "", "")).execute())).isFalse();
    }

    @Test
    public void activateAccount() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.activateAccount(new ReqAccountActivate("","")).execute())).isFalse();
    }

    @Test
    public void accountVerification() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.accountVerification(new ReqAccountVerification("")).execute())).isFalse();
    }


}