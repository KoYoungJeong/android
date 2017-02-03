package com.tosslab.jandi.app.network.client.member;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MemberApiDeprecatedTest {

    private MemberApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(MemberApi.Api.class);
    }

    @Test
    public void getMemberInfo() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getMemberInfo(1,1))).isFalse();

    }
}