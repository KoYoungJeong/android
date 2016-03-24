package com.tosslab.jandi.app.network.client.messages.search;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ResMessageSearch;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by tee on 15. 6. 16..
 */
@AuthorizedHeader
public interface MessagesSearchApiV2Client {

    // Message Search
    @GET("/teams/{teamId}/search/messages")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessageSearch searchMessages(@Path("teamId") long teamId, @Query("q") String query, @Query("page") int page,
                                    @Query("perPage") int perPage, @Query("writerId") long writerId, @Query("entityId") long entityId);

    @GET("/teams/{teamId}/search/messages")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessageSearch searchMessagesByEntityId(@Path("teamId") long teamId, @Query("q") String query, @Query("page") int page,
                                              @Query("perPage") int perPage, @Query("entityId") long entityId);

    @GET("/teams/{teamId}/search/messages")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessageSearch searchMessagesByWriterId(@Path("teamId") long teamId, @Query("q") String query, @Query("page") int page,
                                              @Query("perPage") int perPage, @Query("writerId") long writerId);

    @GET("/teams/{teamId}/search/messages")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessageSearch searchMessages(@Path("teamId") long teamId, @Query("q") String query, @Query("page") int page,
                                    @Query("perPage") int perPage);

}
