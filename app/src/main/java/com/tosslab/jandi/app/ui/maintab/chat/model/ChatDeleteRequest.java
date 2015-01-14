package com.tosslab.jandi.app.ui.maintab.chat.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.chat.ChatsApiClient;
import com.tosslab.jandi.app.network.client.chat.ChatsApiClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

/**
 * Created by Steve SeongUg Jung on 15. 1. 14..
 */
public class ChatDeleteRequest implements Request<ResCommon> {

    private final Context context;
    private final int memberId;
    private final int entityId;
    private final ChatsApiClient chatsApiClient;

    private ChatDeleteRequest(Context context, int memberId, int entityId, ChatsApiClient chatsApiClient) {
        this.context = context;
        this.memberId = memberId;
        this.entityId = entityId;
        this.chatsApiClient = chatsApiClient;
    }

    public static ChatDeleteRequest create(Context context, int memberId, int entityId) {
        return new ChatDeleteRequest(context, memberId, entityId, new ChatsApiClient_(context));
    }


    @Override
    public ResCommon request() throws JandiNetworkException {
        chatsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
        return chatsApiClient.deleteChat(memberId, entityId);
    }
}
