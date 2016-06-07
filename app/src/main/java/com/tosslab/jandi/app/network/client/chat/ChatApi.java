package com.tosslab.jandi.app.network.client.chat;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit2.Call;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public class ChatApi extends ApiTemplate<ChatApi.Api> {
    public ChatApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResCommon deleteChat(long myMemberId, long entityId) throws RetrofitException {
        return call(() -> getApi().deleteChat(myMemberId, entityId));
    }

    interface Api {

        @HTTP(path = "members/{memberId}/chats/{entityId}", hasBody = true, method = "DELETE")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deleteChat(@Path("memberId") long myMemberId, @Path("entityId") long entityId);

    }
}
