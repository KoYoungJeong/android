package com.tosslab.jandi.app.network.client.messages.comments;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqModifyComment;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ResCommon;

/**
 * Created by tee on 15. 6. 23..
 */
public interface ICommentsApiLoader {

    IExecutor<ResCommon> loadSendMessageCommentByCommentsApi(int messageId, int teamId, ReqSendComment ReqSendComment);

    IExecutor<ResCommon> loadModifyMessageCommentByCommentsApi(ReqModifyComment comment, int messageId, int commentId);

    IExecutor<ResCommon> loadDeleteMessageCommentByCommentsApi(int teamId, int messageId, int commentId);

}
