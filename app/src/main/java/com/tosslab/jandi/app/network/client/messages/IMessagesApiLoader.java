package com.tosslab.jandi.app.network.client.messages;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.util.List;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IMessagesApiLoader {

    IExecutor<ResFileDetail> loadGetFileDetailByMessagesApiAuth(long teamId, long messageId);

    IExecutor<ResCommon> loadShareMessageByMessagesApiAuth(ReqShareMessage share, long messageId);

    IExecutor<ResCommon> loadUnshareMessageByMessagesApiAuth(ReqUnshareMessage share, long messageId);

    IExecutor<List<ResMessages.Link>> getRoomUpdateMessageByMessagesApiAuth(long teamId,
                                                                            long roomId,
                                                                            long currentLinkId);


}
