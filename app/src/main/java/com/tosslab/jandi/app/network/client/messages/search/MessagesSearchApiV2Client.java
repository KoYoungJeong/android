package com.tosslab.jandi.app.network.client.messages.search;

import com.tosslab.jandi.app.network.models.ResMessageSearch;

import org.androidannotations.annotations.rest.RequiresAuthentication;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by tee on 15. 6. 16..
 */
public interface MessagesSearchApiV2Client {

    // Message Search
    @GET("/teams/{teamId}/search/messages")
    ResMessageSearch searchMessages(@Path("teamId") int teamId, @Query("q") String query, @Query("page") int page,
                                    @Query("perPage") int perPage, @Query("writeId") int writerId,@Query("entityId") int entityId);

    @GET("/teams/{teamId}/search/messages")
    ResMessageSearch searchMessagesByEntityId(@Path("teamId") int teamId, @Query("q") String query, @Query("page") int page,
                                              @Query("perPage") int perPage, @Query("entityId") int entityId);

    @GET("/teams/{teamId}/search/messages")
    ResMessageSearch searchMessagesByWriterId(@Path("teamId") int teamId, @Query("q") String query, @Query("page") int page,
                                              @Query("perPage") int perPage, @Query("writeId") int writeId);

    @GET("/teams/{teamId}/search/messages")
    ResMessageSearch searchMessages(@Path("teamId") int teamId, @Query("q") String query, @Query("page") int page,
                                    @Query("perPage") int perPage);

}
