package com.tosslab.jandi.app.network.client.sticker;

import com.tosslab.jandi.app.network.manager.RestApiClient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;

import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by tee on 15. 6. 23..
 */
@AuthorizedHeader
public interface StickerApiV2Client {

    @POST("/stickers")
    ResCommon sendSticker(@Body ReqSendSticker reqSendSticker);

    @POST("/stickers/comment")
    ResCommon sendStickerComment(@Body ReqSendSticker reqSendSticker);

}
