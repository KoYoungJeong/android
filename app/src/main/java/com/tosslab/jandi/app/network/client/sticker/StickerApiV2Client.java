package com.tosslab.jandi.app.network.client.sticker;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;

import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.POST;

/**
 * Created by tee on 15. 6. 23..
 */
@AuthorizedHeader
public interface StickerApiV2Client {

    @POST("/stickers")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon sendSticker(@Body ReqSendSticker reqSendSticker);

    @POST("/stickers/comment")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon sendStickerComment(@Body ReqSendSticker reqSendSticker);

}
