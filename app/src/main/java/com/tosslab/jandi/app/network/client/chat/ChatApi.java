package com.tosslab.jandi.app.network.client.chat;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.start.Chat;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class ChatApi extends ApiTemplate<ChatApi.Api> {
    @Inject
    public ChatApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResCommon deleteChat(long teamId, long companionId) throws RetrofitException {
        return call(() -> getApi().deleteChat(teamId, companionId));
    }

    public Chat createChat(long teamId, long memberId) throws RetrofitException {
        Map<String, Long> map = new HashMap<>();
        map.put("memberId", memberId);
        return call(() -> getApi().createChat(teamId, map));
    }

    interface Api {
        @DELETE("teams/{teamId}/chats/{companionId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deleteChat(@Path("teamId") long teamId, @Path("companionId") long companionId);

        @POST("teams/{teamId}/chats")
        Call<Chat> createChat(@Path("teamId") long teamId, @Body Map<String, Long> map);
    }
}
