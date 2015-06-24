package com.tosslab.jandi.app.network.client.messages;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IMessagesApiLoader {

    IExecutor loadGetFileDetailByMessagesApiAuth(int teamId, int messageId);

    IExecutor loadShareMessageByMessagesApiAuth(ReqShareMessage share, int messageId);

    IExecutor loadUnshareMessageByMessagesApiAuth(ReqUnshareMessage share, int messageId);

}
