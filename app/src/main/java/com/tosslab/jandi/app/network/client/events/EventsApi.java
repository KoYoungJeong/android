package com.tosslab.jandi.app.network.client.events;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResEventHistory;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public class EventsApi extends ApiTemplate<EventsApi.Api> {
    @Inject
    public EventsApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResEventHistory getEventHistory(long ts, long memberId,int size) throws RetrofitException {
        return call(() -> getApi().getEventHistory(ts, memberId, size));
    }

    public ResEventHistory getEventHistory(long ts, long memberId) throws RetrofitException {
        return call(() -> getApi().getEventHistory(ts, memberId));
    }


    interface Api {
        @GET("events")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResEventHistory> getEventHistory(@Query("ts") long ts, @Query("memberId") long memberId,
                                              @Query("size") int size);

        @GET("events?size=10000")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResEventHistory> getEventHistory(@Query("ts") long ts, @Query("memberId") long memberId);

    }
}
