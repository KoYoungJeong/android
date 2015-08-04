package com.tosslab.jandi.app.network.client.messages.comments;

import com.tosslab.jandi.app.network.models.ReqModifyComment;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 23..
 */
public interface ICommentsApiAuth {

    ResCommon sendMessageCommentByCommentsApi(int messageId, int teamId, ReqSendComment reqSendComment) throws RetrofitError;

    ResCommon modifyMessageCommentByCommentsApi(ReqModifyComment comment, int messageId, int commentId) throws RetrofitError;

    ResCommon deleteMessageCommentByCommentsApi(int teamId, int messageId, int commentId) throws RetrofitError;

}
