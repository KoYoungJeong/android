package com.tosslab.jandi.app.network.client.chat;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResCommon;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class ChatApi extends ApiTemplate<ChatApi.Api> {
    public ChatApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResCommon deleteChat(long myMemberId, long entityId) throws RetrofitException {
        return call(() -> getApi().deleteChat(myMemberId, entityId));
    }

    public ResCommon createChat(long teamId, long memberId) throws RetrofitException {
        Map<String, Long> map = new HashMap<>();
        map.put("memberId", memberId);
        return call(() -> getApi().createChat(teamId, map));
    }

    interface Api {

        @HTTP(path = "members/{memberId}/chats/{entityId}", hasBody = true, method = "DELETE")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deleteChat(@Path("memberId") long myMemberId, @Path("entityId") long entityId);

        @POST("teams/{teamId}/chats")
        Call<ResCommon> createChat(@Path("teamId") long teamId, @Body Map<String, Long> map);
    }
}
