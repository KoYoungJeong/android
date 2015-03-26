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

        int searchType = getSearchType(reqMessageSearchQeury);

        int teamId = reqMessageSearchQeury.getTeamId();
        String query = reqMessageSearchQeury.getQuery();
        int page = reqMessageSearchQeury.getPage();
        int perPage = reqMessageSearchQeury.getPerPage();
        int writerId = reqMessageSearchQeury.getWriterId();
        int entityId = reqMessageSearchQeury.getEntityId();
        switch (searchType) {
            case 0x11:  // condition with writer & entity
                return messageSearchApiClient.searchMessages(teamId, query, page, perPage, writerId, entityId);
            case 0x10:  // condition with entity
                return messageSearchApiClient.searchMessagesByEntityId(teamId, query, page, perPage, entityId);
            case 0x01:  // condition with writer
                return messageSearchApiClient.searchMessagesByWriterId(teamId, query, page, perPage, writerId);
            default:
            case 0x00:  // all type
                return messageSearchApiClient.searchMessages(teamId, query, page, perPage);
        }

    }

    private int getSearchType(ReqMessageSearchQeury reqMessageSearchQeury) {
        // Calc Bit.
        int type = 0;    // 0000
        if (reqMessageSearchQeury.getWriterId() > 0) {
            type |= 0x01;   // 0001
        }

        if (reqMessageSearchQeury.getEntityId() > 0) {
            type |= 0x10;  // 0010
        }

        return type;
    }
}
