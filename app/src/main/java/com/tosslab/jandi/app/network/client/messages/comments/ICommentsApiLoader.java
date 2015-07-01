package com.tosslab.jandi.app.network.client.messages.comments;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ResCommon;

/**
 * Created by tee on 15. 6. 23..
 */
public interface ICommentsApiLoader {

    IExecutor<ResCommon> loadSendMessageCommentByCommentsApi(ReqSendComment comment, int messageId);

    IExecutor<ResCommon> loadModifyMessageCommentByCommentsApi(ReqSendComment comment, int messageId, int commentId);

    IExecutor<ResCommon> loadDeleteMessageCommentByCommentsApi(int teamId, int messageId, int commentId);

}
