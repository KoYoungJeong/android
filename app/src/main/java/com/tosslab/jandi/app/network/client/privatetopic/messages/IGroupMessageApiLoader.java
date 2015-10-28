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

    IExecutor<ResMessages> loadGetGroupMessagesByGroupMessageApi(int teamId, int groupId, int fromId, int count);

    IExecutor<ResMessages> loadGetGroupMessagesByGroupMessageApi(int teamId, int groupId);

    IExecutor<ResUpdateMessages> loadGetGroupMessagesUpdatedByGroupMessageApi(int teamId, int groupId, int lastLinkId);

    IExecutor<ResMessages> loadGetGroupMessagesUpdatedForMarkerByGroupMessageApi(int teamId, int groupId, int currentLinkId);

    IExecutor<ResMessages> loadGetGroupMessagesUpdatedForMarkerByGroupMessageApi(int teamId, int groupId, int currentLinkId, int count);

    IExecutor<ResMessages> loadGetGroupMarkerMessagesByGroupMessageApi(int teamId, int groupId, int currentLinkId);

    IExecutor<ResCommon> loadSendGroupMessageByGroupMessageApi(int privateGroupId, int teamId, ReqSendMessageV3 reqSendMessageV3);

    IExecutor<ResCommon> loadModifyPrivateGroupMessageByGroupMessageApi(ReqModifyMessage message,
                                                                        int groupId, int messageId);

    IExecutor<ResCommon> loadDeletePrivateGroupMessageByGroupMessageApi(int teamId, int groupId, int messageId);

}
