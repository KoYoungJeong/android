package com.tosslab.jandi.app.ui.search.main.model;

import com.tosslab.jandi.app.ui.search.to.SearchKeyword;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Config(manifest = "app/src/main/AndroidManifest.xml", sdk =18)
@RunWith(RobolectricGradleTestRunner.class)
public class SearchModelTest {

    private SearchModel searchModel;
    private long upsertedId;

    @Before
    public void setUp() throws Exception {
        searchModel = SearchModel_.getInstance_(RuntimeEnvironment.application);
        upsertedId = searchModel.upsertQuery(0, "가나다");
    }

    @Test
    public void testSearchOldQuery() throws Exception {
        List<SearchKeyword> searchKeywords = searchModel.searchOldQuery("가ㄴ");

        assertThat(searchKeywords.size(), is(1));

        searchKeywords = searchModel.searchOldQuery("a가ㄴ");
        assertThat(searchKeywords.size(), is(0));

    }

    @Test
    public void testUpsertQuery() throws Exception {
        long newId = searchModel.upsertQuery(0, "가나다");

        assertThat(newId, is(upsertedId));

    }
}