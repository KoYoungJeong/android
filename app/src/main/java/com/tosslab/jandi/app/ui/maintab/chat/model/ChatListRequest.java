package com.tosslab.jandi.app.ui.maintab.chat.model;

import android.content.Context;

import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.network.client.chat.ChatApiV2Client;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.manager.RestApiClient.RestAdapterFactory.converter.JacksonConverter;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.codehaus.jackson.map.ObjectMapper;

import java.util.List;

import retrofit.RestAdapter;

/**
 * Created by Steve SeongUg Jung on 15. 1. 13..
 */
public class ChatListRequest implements Request<List<ResChat>> {

    private final Context context;
    private final int memberId;
    //    private ChatsApiClient chatsApiClient;
    RestAdapter restAdapter;

//    private ChatListRequest(Context context, int memberId, ChatsApiClient chatsApiClient) {
//        this.context = context;
//        this.memberId = memberId;
//        this.chatsApiClient = chatsApiClient;
//    }

    private ChatListRequest(Context context, int memberId) {
        this.context = context;
        this.memberId = memberId;

        JacksonConverter converter = new JacksonConverter(new ObjectMapper());

        restAdapter = new RestAdapter.Builder()
                .setRequestInterceptor(request -> {
                    request.addHeader("Authorization", TokenUtil.getRequestAuthentication().getHeaderValue());
                })
                .setConverter(converter)
                .setEndpoint(JandiConstantsForFlavors.SERVICE_ROOT_URL + "inner-api")
                .build();


    }

    public static ChatListRequest create(Context context, int memberId) {
        //return new ChatListRequest(context, memberId, new ChatsApiClient_(context));
        return new ChatListRequest(context, memberId);
    }

    @Override
    public List<ResChat> request() throws JandiNetworkException {
//        chatsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
//        return chatsApiClient.getChatList(memberId);
        return restAdapter.create(ChatApiV2Client.class).getChatList(memberId);
    }
}
