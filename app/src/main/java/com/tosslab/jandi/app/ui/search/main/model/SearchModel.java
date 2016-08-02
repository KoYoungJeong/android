package com.tosslab.jandi.app.ui.search.main.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.database.search.JandiSearchDatabaseManager;
import com.tosslab.jandi.app.network.client.teams.search.SearchApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.network.models.search.ResSearch;
import com.tosslab.jandi.app.ui.search.to.SearchKeyword;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
@EBean
public class SearchModel {

    @RootContext
    Context context;

    public List<SearchKeyword> searchOldQuery(String text) {
        return JandiSearchDatabaseManager.getInstance(context).searchKeywords(0, text);
    }

    public long upsertQuery(int type, String text) {
        if (TextUtils.isEmpty(text)) {
            return -1;
        }

        return JandiSearchDatabaseManager.getInstance(context)
                .upsertSearchKeyword(new SearchKeyword(type, text));
    }

}
