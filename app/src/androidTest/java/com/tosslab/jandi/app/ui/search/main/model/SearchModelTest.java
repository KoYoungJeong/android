package com.tosslab.jandi.app.ui.search.main.model;

import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.AndroidJUnitRunner;
import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.search.to.SearchKeyword;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by tonyjs on 15. 11. 26..
 */
@RunWith(AndroidJUnit4.class)
public class SearchModelTest {

    SearchModel searchModel;

    @Before
    public void setup() throws Exception {
        searchModel = SearchModel_.getInstance_(JandiApplication.getContext());
    }

    @Test
    public void testSearchOldQuery() throws Exception {
        searchModel.upsertQuery(0, null);

        List<SearchKeyword> searchKeywords = searchModel.searchOldQuery(null);

        assertTrue(searchKeywords.isEmpty());
    }

    @Test
    public void testUpsertQuery() throws Exception {
        String query = "r";

        searchModel.upsertQuery(0, query);
        // 중복된 경우 테스트
        searchModel.upsertQuery(0, query);

        List<SearchKeyword> searchKeywords = searchModel.searchOldQuery(query);

        Log.d("Test", searchKeywords.toString());

        assertTrue((!searchKeywords.isEmpty()
                && searchKeywords.size() == 1
                && !TextUtils.isEmpty(searchKeywords.get(0).getKeyword())
                && searchKeywords.get(0).getKeyword().equals(query)));
    }
}