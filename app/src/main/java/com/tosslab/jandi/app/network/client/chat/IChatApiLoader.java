package com.tosslab.jandi.app.network.client.chat;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IChatApiLoader {

    IExecutor loadGetChatListByChatApi(int memberId);

    IExecutor loadDeleteChatByChatApi(int memberId, int entityId);

}
