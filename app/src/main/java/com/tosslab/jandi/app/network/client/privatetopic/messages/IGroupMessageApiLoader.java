package com.tosslab.jandi.app.network.client.privatetopic.messages;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IGroupMessageApiLoader {

    Executor<ResMessages> loadGetGroupMessagesByGroupMessageApi(long teamId, long groupId, long fromId, int count);

    Executor<ResMessages> loadGetGroupMessagesByGroupMessageApi(int teamId, int groupId);

    Executor<ResUpdateMessages> loadGetGroupMessagesUpdatedByGroupMessageApi(int teamId, int groupId, int lastLinkId);

    Executor<ResMessages> loadGetGroupMessagesUpdatedForMarkerByGroupMessageApi(long teamId, long groupId, long currentLinkId);

    Executor<ResMessages> loadGetGroupMessagesUpdatedForMarkerByGroupMessageApi(long teamId, long groupId, long currentLinkId, int count);

    Executor<ResMessages> loadGetGroupMarkerMessagesByGroupMessageApi(long teamId, long groupId, long currentLinkId);

    Executor<ResCommon> loadSendGroupMessageByGroupMessageApi(long privateGroupId, long teamId, ReqSendMessageV3 reqSendMessageV3);

    Executor<ResCommon> loadModifyPrivateGroupMessageByGroupMessageApi(ReqModifyMessage message,
                                                                        int groupId, int messageId);

    Executor<ResCommon> loadDeletePrivateGroupMessageByGroupMessageApi(long teamId, long groupId, long messageId);

}
