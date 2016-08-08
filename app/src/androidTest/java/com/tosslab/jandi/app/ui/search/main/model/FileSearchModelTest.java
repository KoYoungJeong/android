package com.tosslab.jandi.app.ui.search.main.model;

import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.database.search.JandiSearchDatabaseManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by tonyjs on 15. 11. 26..
 */
@RunWith(AndroidJUnit4.class)
public class FileSearchModelTest {

    FileSearchModel fileSearchModel;

    @Before
    public void setup() throws Exception {
        fileSearchModel = new FileSearchModel();
    }

    @Test
    public void testSearchOldQuery() throws Exception {
        fileSearchModel.upsertQuery(null);

        List<String> searchKeywords = fileSearchModel.searchOldQuery(null);

        assertTrue(searchKeywords.isEmpty());
    }

    @Test
    public void testUpsertQuery() throws Exception {
        String query = "r";

        fileSearchModel.upsertQuery(query);
        // 중복된 경우 테스트
        fileSearchModel.upsertQuery(query);

        List<String> searchKeywords = fileSearchModel.searchOldQuery(query);

        Log.d("Test", searchKeywords.toString());

        assertTrue((!searchKeywords.isEmpty()
                && searchKeywords.size() == 1
                && !TextUtils.isEmpty(searchKeywords.get(0))
                && searchKeywords.get(0).equals(query)));
    }

    @Test
    public void testSearchOldAllQuery() throws Exception {
        fileSearchModel.upsertQuery("abcde");
        // 중복된 경우 테스트
        fileSearchModel.upsertQuery("defgh");

        List<String> searchKeywords = JandiSearchDatabaseManager.getInstance(JandiApplication.getContext())
                .getSearchAllHistory();

        Log.d("text", searchKeywords.toString());

        assertTrue(!searchKeywords.isEmpty());
    }

}