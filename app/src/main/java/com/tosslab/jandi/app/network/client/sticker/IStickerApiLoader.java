package com.tosslab.jandi.app.network.client.sticker;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IStickerApiLoader {

    IExecutor loadSendStickerByStickerApi(ReqSendSticker reqSendSticker);

    IExecutor loadSendStickerCommentByStickerApi(ReqSendSticker reqSendSticker);

}
