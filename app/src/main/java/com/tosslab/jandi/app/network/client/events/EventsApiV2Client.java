package com.tosslab.jandi.app.network.client.events;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ResEventHistory;

import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;

/**
 * Created by tee on 15. 11. 17..
 */
@AuthorizedHeader
public interface EventsApiV2Client {
    @GET("/events")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResEventHistory getEventHistory(@Query("ts") long ts, @Query("memberId") Integer memberId,
                                    @Query("eventType") String eventType, @Query("size") Integer size);

}
