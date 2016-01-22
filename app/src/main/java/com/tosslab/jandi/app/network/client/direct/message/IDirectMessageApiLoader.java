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

    IExecutor<ResMessages> loadGetDirectMessagesByDirectMessageApi(long teamId, long userId, long fromId, int count);

    IExecutor<ResMessages> loadGetDirectMessagesByDirectMessageApi(int teamId, int userId);

    IExecutor<ResUpdateMessages> loadGetDirectMessagesUpdatedByDirectMessageApi(int teamId, int userId, int timeAfter);

    IExecutor<ResMessages> loadGetDirectMessagesUpdatedForMarkerByDirectMessageApi(long teamId, long userId, long currentLinkId);

    IExecutor<ResMessages> loadGetDirectMessagesUpdatedForMarkerByDirectMessageApi(long teamId, long userId, long currentLinkId, int count);

    IExecutor<ResMessages> loadGetDirectMarkerMessagesByDirectMessageApi(long teamId, long userId, long currentLinkId);

    IExecutor<ResCommon> loadSendDirectMessageByDirectMessageApi(long userId, long teamId,
                                                                 ReqSendMessageV3 reqSendMessageV3);

    IExecutor<ResCommon> loadModifyDirectMessageByDirectMessageApi(ReqModifyMessage message,
                                                                   int userId, int messageId);

    IExecutor<ResCommon> loadDeleteDirectMessageByDirectMessageApi(long teamId, long userId, long messageId);

}
