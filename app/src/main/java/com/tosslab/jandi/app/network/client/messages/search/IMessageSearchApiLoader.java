package com.tosslab.jandi.app.network.client.messages.search;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ResMessageSearch;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IMessageSearchApiLoader {

    IExecutor<ResMessageSearch> loadSearchMessagesByMessageSearchApi(long teamId, String query, int page, int perPage, long writerId, long entityId);

    IExecutor<ResMessageSearch> loadSearchMessagesByEntityIdByMessageSearchApi(long teamId, String query, int page, int perPage, long entityId);

    IExecutor<ResMessageSearch> loadSearchMessagesByWriterIdByMessageSearchApi(long teamId, String query, int page, int perPage, long writerId);

    IExecutor<ResMessageSearch> loadSearchMessagesByMessageSearchApi(long teamId, String query, int page, int perPage);

}
