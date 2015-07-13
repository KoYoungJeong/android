package com.tosslab.jandi.app.network.client.messages.search;

import com.tosslab.jandi.app.network.models.ResMessageSearch;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IMessageSearchApiAuth {

    ResMessageSearch searchMessagesByMessageSearchApi(int teamId, String query, int page, int perPage, int writerId, int entityId) throws RetrofitError;

    ResMessageSearch searchMessagesByEntityIdByMessageSearchApi(int teamId, String query, int page, int perPage, int entityId) throws RetrofitError;

    ResMessageSearch searchMessagesByWriterIdByMessageSearchApi(int teamId, String query, int page, int perPage, int writerId) throws RetrofitError;

    ResMessageSearch searchMessagesByMessageSearchApi(int teamId, String query, int page, int perPage) throws RetrofitError;

}
