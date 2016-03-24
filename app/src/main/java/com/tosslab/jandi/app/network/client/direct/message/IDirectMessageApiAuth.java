package com.tosslab.jandi.app.network.client.direct.message;

import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;



/**
 * Created by tee on 15. 6. 23..
 */
public interface IDirectMessageApiAuth {

    ResMessages getDirectMessagesByDirectMessageApi(long teamId, long userId, long fromId, int count) throws IOException;

    ResMessages getDirectMessagesByDirectMessageApi(int teamId, int userId) throws IOException;

    ResUpdateMessages getDirectMessagesUpdatedByDirectMessageApi(int teamId, int userId, int timeAfter) throws IOException;

    ResMessages getDirectMessagesUpdatedForMarkerByDirectMessageApi(long teamId, long userId, long currentLinkId) throws IOException;

    ResMessages getDirectMessagesUpdatedForMarkerByDirectMessageApi(long teamId, long userId, long currentLinkId, int count) throws IOException;

    ResMessages getDirectMarkerMessagesByDirectMessageApi(long teamId, long userId, long currentLinkId) throws IOException;

    ResCommon sendDirectMessageByDirectMessageApi(long userId, long teamId,
                                                  ReqSendMessageV3 reqSendMessageV3) throws IOException;

    ResCommon modifyDirectMessageByDirectMessageApi(ReqModifyMessage message,
                                                    int userId, int messageId) throws IOException;

    ResCommon deleteDirectMessageByDirectMessageApi(long teamId, long userId, long messageId) throws IOException;

}
