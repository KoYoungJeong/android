package com.tosslab.jandi.app.network.client.chat;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitAdapterBuilder;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.network.models.ResCommon;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public class ChatApi extends ApiTemplate<ChatApi.Api> {
    public ChatApi(RetrofitAdapterBuilder retrofitAdapterBuilder) {
        super(Api.class, retrofitAdapterBuilder);
    }

    public List<ResChat> getChatList(long memberId) throws RetrofitException {
        return call(() -> getApi().getChatList(memberId));
    }

    public ResCommon deleteChat(long teamId, long entityId) throws RetrofitException {
        return call(() -> getApi().deleteChat(teamId, entityId));
    }

    interface Api {

        @GET("members/{memberId}/chats")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<List<ResChat>> getChatList(@Path("memberId") long memberId);

        @HTTP(path = "members/{memberId}/chats/{entityId}", hasBody = true, method = "DELETE")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deleteChat(@Path("memberId") long teamId, @Path("entityId") long entityId);

    }
}
