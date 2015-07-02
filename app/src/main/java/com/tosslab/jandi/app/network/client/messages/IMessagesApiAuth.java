package com.tosslab.jandi.app.network.client.messages;

import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFileDetail;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IMessagesApiAuth {

    ResFileDetail getFileDetailByMessagesApiAuth(int teamId, int messageId);

    ResCommon shareMessageByMessagesApiAuth(ReqShareMessage share, int messageId);

    ResCommon unshareMessageByMessagesApiAuth(ReqUnshareMessage share, int messageId);

}
