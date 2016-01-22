package com.tosslab.jandi.app.network.client.privatetopic.messages;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IGroupMessageApiLoader {

    IExecutor<ResMessages> loadGetGroupMessagesByGroupMessageApi(long teamId, long groupId, long fromId, int count);

    IExecutor<ResMessages> loadGetGroupMessagesByGroupMessageApi(int teamId, int groupId);

    IExecutor<ResUpdateMessages> loadGetGroupMessagesUpdatedByGroupMessageApi(int teamId, int groupId, int lastLinkId);

    IExecutor<ResMessages> loadGetGroupMessagesUpdatedForMarkerByGroupMessageApi(long teamId, long groupId, long currentLinkId);

    IExecutor<ResMessages> loadGetGroupMessagesUpdatedForMarkerByGroupMessageApi(long teamId, long groupId, long currentLinkId, int count);

    IExecutor<ResMessages> loadGetGroupMarkerMessagesByGroupMessageApi(long teamId, long groupId, long currentLinkId);

    IExecutor<ResCommon> loadSendGroupMessageByGroupMessageApi(long privateGroupId, long teamId, ReqSendMessageV3 reqSendMessageV3);

    IExecutor<ResCommon> loadModifyPrivateGroupMessageByGroupMessageApi(ReqModifyMessage message,
                                                                        int groupId, int messageId);

    IExecutor<ResCommon> loadDeletePrivateGroupMessageByGroupMessageApi(long teamId, long groupId, long messageId);

}
