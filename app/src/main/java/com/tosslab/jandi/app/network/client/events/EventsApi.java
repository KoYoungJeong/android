package com.tosslab.jandi.app.network.client.events;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResEventHistory;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public class EventsApi extends ApiTemplate<EventsApi.Api> {
    public EventsApi() {
        super(Api.class);
    }

    public ResEventHistory getEventHistory(long ts, long memberId,
                                           String eventType, Integer size) throws RetrofitException {
        return call(() -> getApi().getEventHistory(ts, memberId, eventType, size));
    }


    interface Api {
        @GET("/events")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResEventHistory> getEventHistory(@Query("ts") long ts, @Query("memberId") long memberId,
                                              @Query("eventType") String eventType, @Query("size") Integer size);

    }
}
