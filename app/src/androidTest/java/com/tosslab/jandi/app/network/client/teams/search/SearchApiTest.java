package com.tosslab.jandi.app.network.client.teams.search;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.network.models.search.ResSearch;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by tee on 16. 7. 20..
 */

@org.junit.runner.RunWith(AndroidJUnit4.class)
public class SearchApiTest {
    @Test
    public void testGetSearch() throws Exception {
        ReqSearch reqSearch = new ReqSearch.Builder()
                .setKeyword("검색")
                .build();

        ResSearch resSearch = null;

        try {
            resSearch = new SearchApi(RetrofitBuilder.getInstance()).getSearch(279, reqSearch);
        } catch (RetrofitException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertNotNull(resSearch);
    }
}