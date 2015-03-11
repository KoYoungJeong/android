package com.tosslab.jandi.app.ui.search.messages.model;

import android.content.Context;

import com.tosslab.jandi.app.network.client.messages.search.MessageSearchApiClient;
import com.tosslab.jandi.app.network.client.messages.search.MessageSearchApiClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ReqMessageSearchQeury;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

/**
 * Created by Steve SeongUg Jung on 15. 3. 11..
 */
public class MessageSearchRequest implements Request<ResMessageSearch> {

    private final Context context;
    private final MessageSearchApiClient messageSearchApiClient;
    private final ReqMessageSearchQeury reqMessageSearchQeury;

    private MessageSearchRequest(Context context, MessageSearchApiClient messageSearchApiClient, ReqMessageSearchQeury reqMessageSearchQeury) {
        this.context = context;
        this.messageSearchApiClient = messageSearchApiClient;
        this.reqMessageSearchQeury = reqMessageSearchQeury;
    }

    public static MessageSearchRequest newInstance(Context context, ReqMessageSearchQeury reqMessageSearchQeury) {
        return new MessageSearchRequest(context, new MessageSearchApiClient_(context), reqMessageSearchQeury);
    }

    @Override
    public ResMessageSearch request() throws JandiNetworkException {
        messageSearchApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
        return messageSearchApiClient.searchMessages(reqMessageSearchQeury.getTeamId(), reqMessageSearchQeury.getQ(), reqMessageSearchQeury.getPage(), reqMessageSearchQeury.getPerPage(), reqMessageSearchQeury.getWriterId(), reqMessageSearchQeury.getEntityId());
    }
}
