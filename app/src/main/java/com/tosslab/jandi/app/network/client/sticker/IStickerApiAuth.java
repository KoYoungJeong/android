package com.tosslab.jandi.app.network.client.sticker;

import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;



/**
 * Created by tee on 15. 6. 23..
 */
public interface IStickerApiAuth {

    ResCommon sendStickerByStickerApi(ReqSendSticker reqSendSticker) throws IOException;

    ResCommon sendStickerCommentByStickerApi(ReqSendSticker reqSendSticker) throws IOException;

    ResCommon deleteStickerCommentByStickerApi(long commentId, long teamId) throws IOException;

    ResCommon deleteStickerByStickerApi(long messageId, long teamId) throws IOException;

}
