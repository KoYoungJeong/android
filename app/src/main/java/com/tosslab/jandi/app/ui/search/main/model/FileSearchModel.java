package com.tosslab.jandi.app.ui.search.main.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.database.search.JandiSearchDatabaseManager;
import com.tosslab.jandi.app.network.client.teams.search.SearchApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.network.models.search.ResSearch;
import com.tosslab.jandi.app.ui.search.to.SearchKeyword;

import java.util.List;

public class FileSearchModel {

    public List<SearchKeyword> searchOldQuery(String text) {
        return JandiSearchDatabaseManager.getInstance(JandiApplication.getContext()).searchKeywords(text);
    }

    public long upsertQuery(int type, String text) {
        if (TextUtils.isEmpty(text)) {
            return -1;
        }

        return JandiSearchDatabaseManager.getInstance(JandiApplication.getContext())
                .upsertSearchKeyword(new SearchKeyword(text));
    }

}
