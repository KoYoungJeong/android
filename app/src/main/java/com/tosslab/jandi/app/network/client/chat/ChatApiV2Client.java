package com.tosslab.jandi.app.network.client.chat;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.RestApiClient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.network.models.ResCommon;

import java.util.List;

import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Path;

/**
 * Created by tee on 15. 6. 16..
 */
@AuthorizedHeader
public interface ChatApiV2Client {

    @GET("/members/{memberId}/chats")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    List<ResChat> getChatList(@Path("memberId") int memberId);

    @DELETE("/members/{memberId}/chats/{entityId}")
    @Headers("Accept :"+ JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon deleteChat(@Path("memberId") int teamId,@Path("memberId") int entityId);

}