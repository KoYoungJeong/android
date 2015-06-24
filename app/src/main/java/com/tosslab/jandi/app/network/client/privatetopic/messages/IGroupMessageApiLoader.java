package com.tosslab.jandi.app.network.client.privatetopic.messages;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessage;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IGroupMessageApiLoader {

    IExecutor loadGetGroupMessagesByGroupMessageApi(int teamId, int groupId, int fromId, int count);

    IExecutor loadGetGroupMessagesByGroupMessageApi(int teamId, int groupId);

    IExecutor loadGetGroupMessagesUpdatedByGroupMessageApi(int teamId, int groupId, int lastLinkId);

    IExecutor loadGetGroupMessagesUpdatedForMarkerByGroupMessageApi(int teamId, int groupId, int currentLinkId);

    IExecutor loadGetGroupMarkerMessagesByGroupMessageApi(int teamId, int groupId, int currentLinkId);

    IExecutor loadSendGroupMessageByGroupMessageApi(ReqSendMessage message, int groupId);

    IExecutor loadModifyPrivateGroupMessageByGroupMessageApi(ReqModifyMessage message,
                                                             int groupId, int messageId);

    IExecutor loadDeletePrivateGroupMessageByGroupMessageApi(int teamId, int groupId, int messageId);

}
