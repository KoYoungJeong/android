package com.tosslab.jandi.app.network.client.sticker;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IStickerApiLoader {

    Executor<ResCommon> loadSendStickerByStickerApi(ReqSendSticker reqSendSticker);

    Executor<ResCommon> loadSendStickerCommentByStickerApi(ReqSendSticker reqSendSticker);

    Executor<ResCommon> loadDeleteStickerCommentByStickerApi(long commentId, long teamId);

    Executor<ResCommon> loadDeleteStickerByStickerApi(long messageId, long teamId);

}
