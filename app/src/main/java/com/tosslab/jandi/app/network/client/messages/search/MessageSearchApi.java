package com.tosslab.jandi.app.network.client.messages.search;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResMessageSearch;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class MessageSearchApi extends ApiTemplate<MessageSearchApi.Api> {
    @Inject
    public MessageSearchApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResMessageSearch searchMessages(long teamId, String query, int page,
                                    int perPage, long writerId, long entityId) throws RetrofitException {
        return call(() -> getApi().searchMessages(teamId, query, page, perPage, writerId, entityId));
    }

    public ResMessageSearch searchMessagesByEntityId(long teamId, String query, int page,
                                              int perPage, long entityId) throws RetrofitException {
        return call(() -> getApi().searchMessagesByEntityId(teamId, query, page, perPage, entityId));
    }

    public ResMessageSearch searchMessagesByWriterId(long teamId, String query, int page,
                                              int perPage, long writerId) throws RetrofitException {
        return call(() -> getApi().searchMessagesByWriterId(teamId, query, page, perPage, writerId));
    }

    public ResMessageSearch searchMessages(long teamId, String query, int page,
                                    int perPage) throws RetrofitException {
        return call(() -> getApi().searchMessages(teamId, query, page, perPage));
    }


    interface Api {

        // Message ReqSearch
        @GET("teams/{teamId}/searchMessages/messages")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessageSearch> searchMessages(@Path("teamId") long teamId, @Query("q") String query, @Query("page") int page,
                                        @Query("perPage") int perPage, @Query("writerId") long writerId, @Query("entityId") long entityId);

        @GET("teams/{teamId}/searchMessages/messages")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessageSearch> searchMessagesByEntityId(@Path("teamId") long teamId, @Query("q") String query, @Query("page") int page,
                                                  @Query("perPage") int perPage, @Query("entityId") long entityId);

        @GET("teams/{teamId}/searchMessages/messages")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessageSearch> searchMessagesByWriterId(@Path("teamId") long teamId, @Query("q") String query, @Query("page") int page,
                                                  @Query("perPage") int perPage, @Query("writerId") long writerId);

        @GET("teams/{teamId}/searchMessages/messages")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessageSearch> searchMessages(@Path("teamId") long teamId, @Query("q") String query, @Query("page") int page,
                                        @Query("perPage") int perPage);

    }
}
