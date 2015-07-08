package com.tosslab.jandi.app.network.client.messages.search;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ResMessageSearch;

import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by tee on 15. 6. 16..
 */
@AuthorizedHeader
public interface MessagesSearchApiV2Client {

    // Message Search
    @GET("/teams/{teamId}/search/messages")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessageSearch searchMessages(@Path("teamId") int teamId, @Query("q") String query, @Query("page") int page,
                                    @Query("perPage") int perPage, @Query("writerId") int writerId, @Query("entityId") int entityId);

    @GET("/teams/{teamId}/search/messages")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessageSearch searchMessagesByEntityId(@Path("teamId") int teamId, @Query("q") String query, @Query("page") int page,
                                              @Query("perPage") int perPage, @Query("entityId") int entityId);

    @GET("/teams/{teamId}/search/messages")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessageSearch searchMessagesByWriterId(@Path("teamId") int teamId, @Query("q") String query, @Query("page") int page,
                                              @Query("perPage") int perPage, @Query("writerId") int writerId);

    @GET("/teams/{teamId}/search/messages")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResMessageSearch searchMessages(@Path("teamId") int teamId, @Query("q") String query, @Query("page") int page,
                                    @Query("perPage") int perPage);

}