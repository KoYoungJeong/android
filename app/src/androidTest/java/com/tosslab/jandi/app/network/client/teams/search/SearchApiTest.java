package com.tosslab.jandi.app.network.client.teams.search;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.network.models.search.ResSearch;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import setup.BaseInitUtil;

/**
 * Created by tee on 16. 7. 20..
 */

@org.junit.runner.RunWith(AndroidJUnit4.class)
public class SearchApiTest {

    private static ResAccountInfo accountInfo;
    private static StartApi startApi;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
        ResAccessToken accessToken = new LoginApi(RetrofitBuilder.getInstance())
                .getAccessToken(ReqAccessToken.createPasswordReqToken(BaseInitUtil.TEST1_EMAIL, BaseInitUtil.TEST_PASSWORD));
        TokenUtil.saveTokenInfoByPassword(accessToken);
        accountInfo = new AccountApi(RetrofitBuilder.getInstance()).getAccountInfo();
        startApi = new StartApi(RetrofitBuilder.getInstance());
        startApi.getInitializeInfo(accountInfo.getMemberships().iterator().next().getTeamId());
    }

    @Test
    public void testGetSearch() throws Exception {
        ReqSearch reqSearch = new ReqSearch.Builder()
                .setKeyword("검색")
                .build();

        ResSearch resSearch = null;

        try {
            resSearch = new SearchApi(
                    RetrofitBuilder.getInstance()).getSearch(accountInfo.getMemberships().iterator().next().getTeamId(), reqSearch);
        } catch (RetrofitException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertNotNull(resSearch);
    }
}