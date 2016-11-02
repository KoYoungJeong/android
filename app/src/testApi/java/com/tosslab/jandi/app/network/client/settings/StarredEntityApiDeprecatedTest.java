package com.tosslab.jandi.app.network.client.settings;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqTeam;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StarredEntityApiDeprecatedTest {

    private StarredEntityApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(StarredEntityApi.Api.class);
    }

    @Test
    public void enableFavorite() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.enableFavorite(1, new ReqTeam(1)).execute())).isFalse();
    }

    @Test
    public void disableFavorite() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.disableFavorite(1, 1).execute())).isFalse();
    }


}