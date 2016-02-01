package com.tosslab.jandi.app.network.client.chat;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.network.models.ResCommon;

import java.util.List;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IChatApiLoader {

    IExecutor<List<ResChat>> loadGetChatListByChatApi(long memberId);

    IExecutor<ResCommon> loadDeleteChatByChatApi(long memberId, long entityId);

}
