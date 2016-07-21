package com.tosslab.jandi.app.ui.search.main_temp.model;

import com.tosslab.jandi.app.network.client.teams.search.SearchApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.network.models.search.ResSearch;

import javax.inject.Inject;

import dagger.Lazy;

/**
 * Created by tee on 16. 7. 20..
 */
public class SearchModel {

    @Inject
    Lazy<SearchApi> searchApi;

    public ResSearch search(int teamId, ReqSearch reqSearch) throws RetrofitException {
        return searchApi.get().getSearch(teamId, reqSearch);
    }

}
