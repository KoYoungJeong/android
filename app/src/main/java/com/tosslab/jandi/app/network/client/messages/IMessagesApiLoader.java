package com.tosslab.jandi.app.network.client.messages;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
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

    Executor<ResFileDetail> loadGetFileDetailByMessagesApiAuth(long teamId, long messageId);

    Executor<ResCommon> loadShareMessageByMessagesApiAuth(ReqShareMessage share, long messageId);

    Executor<ResCommon> loadUnshareMessageByMessagesApiAuth(ReqUnshareMessage share, long messageId);

    Executor<List<ResMessages.Link>> getRoomUpdateMessageByMessagesApiAuth(long teamId,
                                                                            long roomId,
                                                                            long currentLinkId);


}
