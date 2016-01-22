package com.tosslab.jandi.app.network.client.direct.message;

import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IDirectMessageApiAuth {

    ResMessages getDirectMessagesByDirectMessageApi(long teamId, long userId, long fromId, int count) throws RetrofitError;

    ResMessages getDirectMessagesByDirectMessageApi(int teamId, int userId) throws RetrofitError;

    ResUpdateMessages getDirectMessagesUpdatedByDirectMessageApi(int teamId, int userId, int timeAfter) throws RetrofitError;

    ResMessages getDirectMessagesUpdatedForMarkerByDirectMessageApi(long teamId, long userId, long currentLinkId) throws RetrofitError;

    ResMessages getDirectMessagesUpdatedForMarkerByDirectMessageApi(long teamId, long userId, long currentLinkId, int count) throws RetrofitError;

    ResMessages getDirectMarkerMessagesByDirectMessageApi(long teamId, long userId, long currentLinkId) throws RetrofitError;

    ResCommon sendDirectMessageByDirectMessageApi(long userId, long teamId,
                                                  ReqSendMessageV3 reqSendMessageV3) throws RetrofitError;

    ResCommon modifyDirectMessageByDirectMessageApi(ReqModifyMessage message,
                                                    int userId, int messageId) throws RetrofitError;

    ResCommon deleteDirectMessageByDirectMessageApi(long teamId, long userId, long messageId) throws RetrofitError;

}
