package com.tosslab.jandi.app.ui.search.main_temp.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.main.LoginApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.network.models.search.ResSearch;
import com.tosslab.jandi.app.utils.TokenUtil;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import dagger.Component;
import setup.BaseInitUtil;

/**
 * Created by tee on 16. 7. 25..
 */
@RunWith(AndroidJUnit4.class)
public class SearchModelTest {

    private static ResAccountInfo accountInfo;
    private static StartApi startApi;
    @Inject
    SearchModel searchModel;

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

    @AfterClass
    public static void tearDownClass() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Before
    public void setUp() throws Exception {
        DaggerSearchModelTest_SearchModelTestComponent.builder()
                .build()
                .inject(this);
    }

    @Test
    public void testSearch() {
        try {
            ReqSearch reqSearch = new ReqSearch.Builder()
                    .setKeyword("검색")
                    .build();
            ResSearch resSearch = searchModel.search(accountInfo.getMemberships().iterator().next().getTeamId(), reqSearch);
            Assert.assertNotNull(resSearch);
        } catch (RetrofitException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Component(modules = ApiClientModule.class)
    public interface SearchModelTestComponent {
        void inject(SearchModelTest test);
    }

}