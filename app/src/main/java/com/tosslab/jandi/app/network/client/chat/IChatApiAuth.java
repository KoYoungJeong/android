package com.tosslab.jandi.app.network.client.chat;

import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.network.models.ResCommon;

import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IChatApiAuth {

    List<ResChat> getChatListByChatApi(long memberId) throws RetrofitError;

    ResCommon deleteChatByChatApi(long memberId, long entityId) throws RetrofitError;

}
