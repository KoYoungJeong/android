package com.tosslab.jandi.app.ui.maintab.chat.model;

import android.content.Context;

import com.tosslab.jandi.app.network.manager.RestApiClient.RestAdapterFactory.converter.JacksonConverter;
import com.tosslab.jandi.app.network.client.chat.ChatApiV2Client;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.codehaus.jackson.map.ObjectMapper;

import retrofit.RestAdapter;

/**
 * Created by Steve SeongUg Jung on 15. 1. 14..
 */
public class ChatDeleteRequest implements Request<ResCommon> {

    private final Context context;
    private final int memberId;
    private final int entityId;
//    private final ChatsApiClient chatsApiClient;

    private RestAdapter restAdapter;

//    private ChatDeleteRequest(Context context, int memberId, int entityId, ChatsApiClient chatsApiClient) {
//        this.context = context;
//        this.memberId = memberId;
//        this.entityId = entityId;
//        this.chatsApiClient = chatsApiClient;
//    }

    private ChatDeleteRequest(Context context,int memberId, int entityId){
        this.context = context;
        this.memberId = memberId;
        this.entityId = entityId;

        JacksonConverter converter = new JacksonConverter(new ObjectMapper());

        restAdapter = new RestAdapter.Builder()
                .setRequestInterceptor(request -> {
                    request.addHeader("Authorization", TokenUtil.getRequestAuthentication().getHeaderValue());
                })
                .setConverter(converter)
                .setEndpoint("http://i2.jandi.io:8888/inner-api")
                .build();


    }

    public static ChatDeleteRequest create(Context context, int memberId, int entityId) {
       // return new ChatDeleteRequest(context, memberId, entityId, new ChatsApiClient_(context));
        return new ChatDeleteRequest(context, memberId, entityId);
    }


    @Override
    public ResCommon request() throws JandiNetworkException {
        //chatsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
        //return chatsApiClient.deleteChat(memberId, entityId);
        return restAdapter.create(ChatApiV2Client.class).deleteChat(memberId, entityId);
    }
}
