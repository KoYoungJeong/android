package com.tosslab.jandi.app.ui.search.file.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.database.search.JandiSearchDatabaseManager;

import java.util.List;

import javax.inject.Inject;

public class FileSearchModel {


    @Inject
    public FileSearchModel() { }


    public List<String> searchOldQuery(String text) {
        return JandiSearchDatabaseManager.getInstance(JandiApplication.getContext()).getSearchKeywords(text);
    }

    public long upsertQuery(String text) {
        if (TextUtils.isEmpty(text)) {
            return -1;
        }

        return JandiSearchDatabaseManager.getInstance(JandiApplication.getContext())
                .upsertSearchKeyword(text);
    }

}
