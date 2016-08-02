package com.tosslab.jandi.app.ui.search.main.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.database.search.JandiSearchDatabaseManager;
import com.tosslab.jandi.app.ui.search.to.SearchKeyword;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
@EBean
public class SearchModel {

    @RootContext
    Context context;

    public List<String> searchOldQuery(String text) {
        return JandiSearchDatabaseManager.getInstance(context).getSearchKeywords(text);
    }

    public long upsertQuery(int type, String text) {
        if (TextUtils.isEmpty(text)) {
            return -1;
        }

        return JandiSearchDatabaseManager.getInstance(context)
                .upsertSearchKeyword(text);
    }

}
