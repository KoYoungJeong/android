package com.tosslab.jandi.app.ui.search.main_temp.model;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.network.models.search.ResSearch;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchTopicRoomData;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import javax.inject.Inject;

import dagger.Component;
import setup.BaseInitUtil;

/**
 * Created by tee on 16. 7. 25..
 */
@RunWith(AndroidJUnit4.class)
public class SearchModelTest {

    @Inject
    SearchModel searchModel;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
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
            ResSearch resSearch = searchModel.searchMessages(TeamInfoLoader.getInstance().getTeamId(), reqSearch);
            Assert.assertNotNull(resSearch);
        } catch (RetrofitException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testGetSearchedTopics() {
        List<SearchTopicRoomData> paticipatedTopics = searchModel.getSearchedTopics("테스트", true);
        List<SearchTopicRoomData> topics = searchModel.getSearchedTopics("테스트", false);

        Assert.assertNotNull(paticipatedTopics);
        Assert.assertNotNull(topics);
    }

    @Component(modules = ApiClientModule.class)
    public interface SearchModelTestComponent {
        void inject(SearchModelTest test);
    }

}