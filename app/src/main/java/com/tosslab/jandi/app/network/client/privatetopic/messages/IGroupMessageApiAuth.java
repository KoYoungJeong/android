package com.tosslab.jandi.app.network.client.privatetopic.messages;

import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IGroupMessageApiAuth {

    ResMessages getGroupMessagesByGroupMessageApi(int teamId, int groupId, int fromId, int count) throws RetrofitError;

    ResMessages getGroupMessagesByGroupMessageApi(int teamId, int groupId) throws RetrofitError;

    ResUpdateMessages getGroupMessagesUpdatedByGroupMessageApi(int teamId, int groupId, int lastLinkId) throws RetrofitError;

    ResMessages getGroupMessagesUpdatedForMarkerByGroupMessageApi(int teamId, int groupId, int
            currentLinkId) throws RetrofitError;

    ResMessages getGroupMessagesUpdatedForMarkerByGroupMessageApi(int teamId,
                                                                  int groupId,
                                                                  int currentLinkId,
                                                                  int count) throws RetrofitError;

    ResMessages getGroupMarkerMessagesByGroupMessageApi(int teamId, int groupId, int currentLinkId) throws RetrofitError;

    ResCommon sendGroupMessageByGroupMessageApi(int privateGroupId, int teamId, ReqSendMessageV3 reqSendMessageV3) throws RetrofitError;

    ResCommon modifyPrivateGroupMessageByGroupMessageApi(ReqModifyMessage message,
                                                         int groupId, int messageId) throws RetrofitError;

    ResCommon deletePrivateGroupMessageByGroupMessageApi(int teamId, int groupId, int messageId) throws RetrofitError;

}
