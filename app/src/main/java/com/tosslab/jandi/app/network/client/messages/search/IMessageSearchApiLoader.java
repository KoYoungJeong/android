package com.tosslab.jandi.app.network.client.messages.search;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
import com.tosslab.jandi.app.network.models.ResMessageSearch;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IMessageSearchApiLoader {

    Executor<ResMessageSearch> loadSearchMessagesByMessageSearchApi(long teamId, String query, int page, int perPage, long writerId, long entityId);

    Executor<ResMessageSearch> loadSearchMessagesByEntityIdByMessageSearchApi(long teamId, String query, int page, int perPage, long entityId);

    Executor<ResMessageSearch> loadSearchMessagesByWriterIdByMessageSearchApi(long teamId, String query, int page, int perPage, long writerId);

    Executor<ResMessageSearch> loadSearchMessagesByMessageSearchApi(long teamId, String query, int page, int perPage);

}
