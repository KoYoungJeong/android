package com.tosslab.jandi.app.network.client.marker;

import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.marker.Marker;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by tee on 2017. 4. 4..
 */

public class MarkerApi extends ApiTemplate<MarkerApi.Api> {
    @Inject
    public MarkerApi(RetrofitBuilder retrofitBuilder) {
        super(MarkerApi.Api.class, retrofitBuilder);
    }

    public List<Marker> getMarkersFromMemberId(long teamId, long memberId) throws RetrofitException {
        return call(() -> getApi().getMarkersFromMemberId(teamId, memberId));
    }

    public List<Marker> getMarkersFromRoomId(long teamId, long roomId) throws RetrofitException {
        return call(() -> getApi().getMarkersFromRoomId(teamId, roomId));
    }

    interface Api {
        @GET("/marker-api/v1/teams/{teamId}/members/{memberId}/markers")
        Call<List<Marker>> getMarkersFromMemberId(@Path("teamId") long teamId, @Path("memberId") long memberId);

        @GET("/marker-api/v1/teams/{teamId}/rooms/{roomId}/markers")
        Call<List<Marker>> getMarkersFromRoomId(@Path("teamId") long teamId, @Path("roomId") long roomId);
    }
}