package com.tosslab.jandi.app.network.client.direct.message;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IDirectMessageApiLoader {

    Executor<ResMessages> loadGetDirectMessagesByDirectMessageApi(long teamId, long userId, long fromId, int count);

    Executor<ResMessages> loadGetDirectMessagesByDirectMessageApi(int teamId, int userId);

    Executor<ResUpdateMessages> loadGetDirectMessagesUpdatedByDirectMessageApi(int teamId, int userId, int timeAfter);

    Executor<ResMessages> loadGetDirectMessagesUpdatedForMarkerByDirectMessageApi(long teamId, long userId, long currentLinkId);

    Executor<ResMessages> loadGetDirectMessagesUpdatedForMarkerByDirectMessageApi(long teamId, long userId, long currentLinkId, int count);

    Executor<ResMessages> loadGetDirectMarkerMessagesByDirectMessageApi(long teamId, long userId, long currentLinkId);

    Executor<ResCommon> loadSendDirectMessageByDirectMessageApi(long userId, long teamId,
                                                                 ReqSendMessageV3 reqSendMessageV3);

    Executor<ResCommon> loadModifyDirectMessageByDirectMessageApi(ReqModifyMessage message,
                                                                   int userId, int messageId);

    Executor<ResCommon> loadDeleteDirectMessageByDirectMessageApi(long teamId, long userId, long messageId);

}
