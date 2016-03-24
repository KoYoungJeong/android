package com.tosslab.jandi.app.network.client.events;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ResEventHistory;

import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by tee on 15. 11. 17..
 */
@AuthorizedHeader
public interface EventsApiV2Client {
    @GET("/events")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResEventHistory getEventHistory(@Query("ts") long ts, @Query("memberId") long memberId,
                                    @Query("eventType") String eventType, @Query("size") Integer size);

}
