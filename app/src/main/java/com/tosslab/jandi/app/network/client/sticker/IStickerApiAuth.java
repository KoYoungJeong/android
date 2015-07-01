package com.tosslab.jandi.app.network.client.sticker;

import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IStickerApiAuth {

    ResCommon sendStickerByStickerApi(ReqSendSticker reqSendSticker) throws RetrofitError;

    ResCommon sendStickerCommentByStickerApi(ReqSendSticker reqSendSticker) throws RetrofitError;

}
