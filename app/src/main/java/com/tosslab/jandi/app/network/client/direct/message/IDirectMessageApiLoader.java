package com.tosslab.jandi.app.network.client.direct.message;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessage;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IDirectMessageApiLoader {

    IExecutor loadGetDirectMessagesByDirectMessageApi(int teamId, int userId, int fromId, int count);

    IExecutor loadGetDirectMessagesByDirectMessageApi(int teamId, int userId);

    IExecutor loadGetDirectMessagesUpdatedByDirectMessageApi(int teamId, int userId, int timeAfter);

    IExecutor loadGetDirectMessagesUpdatedForMarkerByDirectMessageApi(int teamId, int userId, int currentLinkId);

    IExecutor loadGetDirectMarkerMessagesByDirectMessageApi(int teamId, int userId, int currentLinkId);

    IExecutor loadSendDirectMessageByDirectMessageApi(ReqSendMessage message, int userId);

    IExecutor loadModifyDirectMessageByDirectMessageApi(ReqModifyMessage message,
                                                        int userId, int messageId);

    IExecutor loadDeleteDirectMessageByDirectMessageApi(int teamId, int userId, int messageId);

}
