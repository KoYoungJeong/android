package com.tosslab.jandi.app.network.client.privatetopic.messages;

import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;



/**
 * Created by tee on 15. 6. 23..
 */
public interface IGroupMessageApiAuth {

    ResMessages getGroupMessagesByGroupMessageApi(long teamId, long groupId, long fromId, int count) throws IOException;

    ResMessages getGroupMessagesByGroupMessageApi(int teamId, int groupId) throws IOException;

    ResUpdateMessages getGroupMessagesUpdatedByGroupMessageApi(int teamId, int groupId, int lastLinkId) throws IOException;

    ResMessages getGroupMessagesUpdatedForMarkerByGroupMessageApi(long teamId, long groupId, long
            currentLinkId) throws IOException;

    ResMessages getGroupMessagesUpdatedForMarkerByGroupMessageApi(long teamId,
                                                                  long groupId,
                                                                  long currentLinkId,
                                                                  int count) throws IOException;

    ResMessages getGroupMarkerMessagesByGroupMessageApi(long teamId, long groupId, long currentLinkId) throws IOException;

    ResCommon sendGroupMessageByGroupMessageApi(long privateGroupId, long teamId, ReqSendMessageV3 reqSendMessageV3) throws IOException;

    ResCommon modifyPrivateGroupMessageByGroupMessageApi(ReqModifyMessage message,
                                                         int groupId, int messageId) throws IOException;

    ResCommon deletePrivateGroupMessageByGroupMessageApi(long teamId, long groupId, long messageId) throws IOException;

}
