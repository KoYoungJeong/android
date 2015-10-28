package com.tosslab.jandi.app.network.client.direct.message;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IDirectMessageApiLoader {

    IExecutor<ResMessages> loadGetDirectMessagesByDirectMessageApi(int teamId, int userId, int fromId, int count);

    IExecutor<ResMessages> loadGetDirectMessagesByDirectMessageApi(int teamId, int userId);

    IExecutor<ResUpdateMessages> loadGetDirectMessagesUpdatedByDirectMessageApi(int teamId, int userId, int timeAfter);

    IExecutor<ResMessages> loadGetDirectMessagesUpdatedForMarkerByDirectMessageApi(int teamId, int userId, int currentLinkId);

    IExecutor<ResMessages> loadGetDirectMessagesUpdatedForMarkerByDirectMessageApi(int teamId, int userId, int currentLinkId, int count);

    IExecutor<ResMessages> loadGetDirectMarkerMessagesByDirectMessageApi(int teamId, int userId, int currentLinkId);

    IExecutor<ResCommon> loadSendDirectMessageByDirectMessageApi(int userId, int teamId,
                                                                 ReqSendMessageV3 reqSendMessageV3);

    IExecutor<ResCommon> loadModifyDirectMessageByDirectMessageApi(ReqModifyMessage message,
                                                                   int userId, int messageId);

    IExecutor<ResCommon> loadDeleteDirectMessageByDirectMessageApi(int teamId, int userId, int messageId);

}
