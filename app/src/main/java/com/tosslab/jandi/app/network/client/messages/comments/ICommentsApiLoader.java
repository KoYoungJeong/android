package com.tosslab.jandi.app.network.client.messages.comments;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
import com.tosslab.jandi.app.network.models.ReqModifyComment;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ResCommon;

/**
 * Created by tee on 15. 6. 23..
 */
public interface ICommentsApiLoader {

    Executor<ResCommon> loadSendMessageCommentByCommentsApi(long messageId, long teamId, ReqSendComment ReqSendComment);

    Executor<ResCommon> loadModifyMessageCommentByCommentsApi(ReqModifyComment comment, int messageId, int commentId);

    Executor<ResCommon> loadDeleteMessageCommentByCommentsApi(long teamId, long messageId, long commentId);

}
