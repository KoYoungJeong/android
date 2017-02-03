package com.tosslab.jandi.app.network.client.teams.search;

import com.tosslab.jandi.app.OkHttpClientTestFactory;
import com.tosslab.jandi.app.ValidationUtil;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchApiDeprecatedTest {

    private SearchApi.Api api;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        OkHttpClientTestFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        api = RetrofitBuilder.getInstance().create(SearchApi.Api.class);
    }

    @Test
    public void getSearch() throws Exception {
        assertThat(ValidationUtil.isDeprecated(api.getSearchResults(1, new HashMap<>()))).isFalse();
    }


}