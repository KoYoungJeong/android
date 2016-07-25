package com.tosslab.jandi.app.ui.search.main.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.database.search.JandiSearchDatabaseManager;
import com.tosslab.jandi.app.ui.search.to.SearchKeyword;

import java.util.List;

public class FileSearchModel {

    public List<SearchKeyword> searchOldQuery(String text) {
        return JandiSearchDatabaseManager.getInstance(JandiApplication.getContext()).searchKeywords(0, text);
    }

    public long upsertQuery(int type, String text) {
        if (TextUtils.isEmpty(text)) {
            return -1;
        }

        return JandiSearchDatabaseManager.getInstance(JandiApplication.getContext())
                .upsertSearchKeyword(new SearchKeyword(type, text));
    }
}
