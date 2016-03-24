package com.tosslab.jandi.app.network.client.sticker;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.DELETEWithBody;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;

import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by tee on 15. 6. 23..
 */
@AuthorizedHeader
public interface StickerApiV2Client {

    @POST("/stickers")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
    ResCommon sendSticker(@Body ReqSendSticker reqSendSticker);

    @POST("/stickers/comment")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon sendStickerComment(@Body ReqSendSticker reqSendSticker);

    @DELETEWithBody("/stickers/comments/{commentId}")
    ResCommon deleteStickerComment(@Path("commentId") long commentId, @Query("teamId") long teamId);

    @DELETEWithBody("/stickers/messages/{messageId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon deleteSticker(@Path("messageId") long messageId, @Query("teamId") long teamId);

}
