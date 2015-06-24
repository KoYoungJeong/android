package com.tosslab.jandi.app.network.client.messages.comments;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqSendComment;

/**
 * Created by tee on 15. 6. 23..
 */
public interface ICommentsApiLoader {

    IExecutor loadSendMessageCommentByCommentsApi(ReqSendComment comment, int messageId);

    IExecutor loadModifyMessageCommentByCommentsApi(ReqSendComment comment, int messageId, int commentId);

    IExecutor loadDeleteMessageCommentByCommentsApi(int teamId, int messageId, int commentId);

}
