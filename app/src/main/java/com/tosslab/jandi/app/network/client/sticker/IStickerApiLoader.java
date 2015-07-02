package com.tosslab.jandi.app.network.client.sticker;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IStickerApiLoader {

    IExecutor<ResCommon> loadSendStickerByStickerApi(ReqSendSticker reqSendSticker);

    IExecutor<ResCommon> loadSendStickerCommentByStickerApi(ReqSendSticker reqSendSticker);

    IExecutor<ResCommon> loadDeleteStickerCommentByStickerApi(int commentId, int teamId);

    IExecutor<ResCommon> loadDeleteStickerByStickerApi(int messageId, int teamId);

}
