package com.tosslab.jandi.app.network.client.teams.search;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.network.models.search.ResSearch;

import java.util.Map;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Created by tee on 16. 7. 20..
 */
public class SearchApi extends ApiTemplate<SearchApi.Api> {

    @Inject
    public SearchApi(InnerApiRetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResSearch getSearch(long teamId, ReqSearch reqSearch) throws RetrofitException {
        return call(() -> getApi().getSearchResults(teamId, reqSearch.convertMap()));
    }

    interface Api {
        @GET("teams/{teamId}/search")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResSearch> getSearchResults(@Path("teamId") long teamId,
                                         @QueryMap Map<String, String> querys);
    }

}