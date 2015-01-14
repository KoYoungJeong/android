package com.tosslab.jandi.app.ui.maintab.chat.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.chat.ChatsApiClient;
import com.tosslab.jandi.app.network.client.chat.ChatsApiClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 1. 13..
 */
public class ChatListRequest implements Request<List<ResChat>> {

    private final Context context;
    private final int memberId;
    private ChatsApiClient chatsApiClient;

    private ChatListRequest(Context context, int memberId, ChatsApiClient chatsApiClient) {
        this.context = context;
        this.memberId = memberId;
        this.chatsApiClient = chatsApiClient;
    }

    public static ChatListRequest create(Context context, int memberId) {
        return new ChatListRequest(context, memberId, new ChatsApiClient_(context));
    }

    @Override
    public List<ResChat> request() throws JandiNetworkException {
        chatsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
        return chatsApiClient.getChatList(memberId);
    }
}
