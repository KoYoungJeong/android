package com.tosslab.jandi.app.network.client.messages;

import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.util.List;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IMessagesApiAuth {

    ResFileDetail getFileDetailByMessagesApiAuth(long teamId, long messageId);

    ResCommon shareMessageByMessagesApiAuth(ReqShareMessage share, long messageId);

    ResCommon unshareMessageByMessagesApiAuth(ReqUnshareMessage share, long messageId);

    List<ResMessages.Link> getRoomUpdateMessageByMessagesApiAuth(long teamId,
                                                                 long roomId,
                                                                 long currentLinkId);

}
