package com.tosslab.jandi.app.network.client.messages.search;

import com.tosslab.jandi.app.network.models.ResMessageSearch;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IMessageSearchApiAuth {

    ResMessageSearch searchMessagesByMessageSearchApi(long teamId, String query, int page, int perPage, long writerId, long entityId) throws RetrofitError;

    ResMessageSearch searchMessagesByEntityIdByMessageSearchApi(long teamId, String query, int page, int perPage, long entityId) throws RetrofitError;

    ResMessageSearch searchMessagesByWriterIdByMessageSearchApi(long teamId, String query, int page, int perPage, long writerId) throws RetrofitError;

    ResMessageSearch searchMessagesByMessageSearchApi(long teamId, String query, int page, int perPage) throws RetrofitError;

}
