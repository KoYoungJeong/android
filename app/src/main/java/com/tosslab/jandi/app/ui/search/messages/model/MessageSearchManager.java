package com.tosslab.jandi.app.ui.search.messages.model;

import com.tosslab.jandi.app.network.client.messages.search.MessageSearchApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqMessageSearchQeury;
import com.tosslab.jandi.app.network.models.ResMessageSearch;

import javax.inject.Inject;

import dagger.Lazy;


public class MessageSearchManager {

    private final ReqMessageSearchQeury reqMessageSearchQeury;

    @Inject
    Lazy<MessageSearchApi> messageSearchApi;

    private MessageSearchManager(ReqMessageSearchQeury reqMessageSearchQuery) {
        this.reqMessageSearchQeury = reqMessageSearchQuery;
        DaggerApiClientComponent.create().inject(this);
    }

    public static final MessageSearchManager newInstance(ReqMessageSearchQeury reqMessageSearchQuery) {
        return new MessageSearchManager(reqMessageSearchQuery);
    }

    public ResMessageSearch request() throws RetrofitException {

        int searchType = getSearchType(reqMessageSearchQeury);

        long teamId = reqMessageSearchQeury.getTeamId();
        String query = reqMessageSearchQeury.getQuery();
        int page = reqMessageSearchQeury.getPage();
        int perPage = reqMessageSearchQeury.getPerPage();
        long writerId = reqMessageSearchQeury.getWriterId();
        long entityId = reqMessageSearchQeury.getEntityId();

        switch (searchType) {

            case 0x11:  // condition with writer & entity
                return messageSearchApi.get().searchMessages(teamId, query, page, perPage, writerId, entityId);

            case 0x10:  // condition with entity
                return messageSearchApi.get().searchMessagesByEntityId(teamId, query, page, perPage, entityId);

            case 0x01:  // condition with writer
                return messageSearchApi.get().searchMessagesByWriterId(teamId, query, page, perPage, writerId);

            default:

            case 0x00:  // all type
                return messageSearchApi.get().searchMessages(teamId, query, page, perPage);

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
