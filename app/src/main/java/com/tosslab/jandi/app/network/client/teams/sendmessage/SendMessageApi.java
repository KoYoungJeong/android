package com.tosslab.jandi.app.network.client.teams.sendmessage;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqSendMessages;
import com.tosslab.jandi.app.network.models.ResCommon;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by tee on 2016. 10. 12..
 */

public class SendMessageApi extends ApiTemplate<SendMessageApi.Api> {

    @Inject
    public SendMessageApi(InnerApiRetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResCommon sendMessage(long teamId, long roomId,
                                 ReqSendMessages reqSendMessages) throws RetrofitException {
        return call(() -> getApi().sendMessage(teamId, roomId, reqSendMessages));
    }

    interface Api {
        @POST("teams/{teamId}/rooms/{roomId}/messages")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> sendMessage(@Path("teamId") long teamId,
                                    @Path("roomId") long roomId,
                                    @Body ReqSendMessages reqSendMessages);
    }

}