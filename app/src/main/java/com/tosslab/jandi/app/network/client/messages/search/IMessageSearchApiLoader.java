package com.tosslab.jandi.app.network.client.messages.search;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IMessageSearchApiLoader {

    IExecutor loadSearchMessagesByMessageSearchApi(int teamId, String query, int page, int perPage, int writerId, int entityId);

    IExecutor loadSearchMessagesByEntityIdByMessageSearchApi(int teamId, String query, int page, int perPage, int entityId);

    IExecutor loadSearchMessagesByWriterIdByMessageSearchApi(int teamId, String query, int page, int perPage, int writerId);

    IExecutor loadSearchMessagesByMessageSearchApi(int teamId, String query, int page, int perPage);

}
