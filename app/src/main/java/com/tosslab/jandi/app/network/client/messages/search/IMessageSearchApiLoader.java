package com.tosslab.jandi.app.network.client.messages.search;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ResMessageSearch;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IMessageSearchApiLoader {

    IExecutor<ResMessageSearch> loadSearchMessagesByMessageSearchApi(int teamId, String query, int page, int perPage, int writerId, int entityId);

    IExecutor<ResMessageSearch> loadSearchMessagesByEntityIdByMessageSearchApi(int teamId, String query, int page, int perPage, int entityId);

    IExecutor<ResMessageSearch> loadSearchMessagesByWriterIdByMessageSearchApi(int teamId, String query, int page, int perPage, int writerId);

    IExecutor<ResMessageSearch> loadSearchMessagesByMessageSearchApi(int teamId, String query, int page, int perPage);

}
