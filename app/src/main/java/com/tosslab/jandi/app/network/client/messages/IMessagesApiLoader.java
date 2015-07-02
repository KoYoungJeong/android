package com.tosslab.jandi.app.network.client.messages;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFileDetail;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IMessagesApiLoader {

    IExecutor<ResFileDetail> loadGetFileDetailByMessagesApiAuth(int teamId, int messageId);

    IExecutor<ResCommon> loadShareMessageByMessagesApiAuth(ReqShareMessage share, int messageId);

    IExecutor<ResCommon> loadUnshareMessageByMessagesApiAuth(ReqUnshareMessage share, int messageId);

}
