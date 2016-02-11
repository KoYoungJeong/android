package com.tosslab.jandi.app.network.client.messages.comments;

import com.tosslab.jandi.app.network.models.ReqModifyComment;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 23..
 */
public interface ICommentsApiAuth {

    ResCommon sendMessageCommentByCommentsApi(long messageId, long teamId, ReqSendComment reqSendComment) throws RetrofitError;

    ResCommon modifyMessageCommentByCommentsApi(ReqModifyComment comment, int messageId, int commentId) throws RetrofitError;

    ResCommon deleteMessageCommentByCommentsApi(long teamId, long messageId, long commentId) throws RetrofitError;

}
