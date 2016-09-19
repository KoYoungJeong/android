package com.tosslab.jandi.app.network.client.sticker;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class StickerApi extends ApiTemplate<StickerApi.Api> {
    public StickerApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResCommon sendSticker(ReqSendSticker reqSendSticker) throws RetrofitException {
        return call(() -> getApi().sendSticker(reqSendSticker));
    }

    public List<ResMessages.Link> sendStickerComment(ReqSendSticker reqSendSticker) throws RetrofitException {
        return call(() -> getApi().sendStickerComment(reqSendSticker));
    }

    public ResCommon deleteStickerComment(long commentId, long teamId) throws RetrofitException {
        return call(() -> getApi().deleteStickerComment(commentId, teamId));
    }

    public ResCommon deleteSticker(long messageId, long teamId) throws RetrofitException {
        return call(() -> getApi().deleteSticker(messageId, teamId));
    }


    interface Api {

        @POST("stickers")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResCommon> sendSticker(@Body ReqSendSticker reqSendSticker);

        @POST("stickers/comment")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<List<ResMessages.Link>> sendStickerComment(@Body ReqSendSticker reqSendSticker);

        @HTTP(path = "stickers/comments/{commentId}", method = "DELETE", hasBody = true)
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deleteStickerComment(@Path("commentId") long commentId, @Query("teamId") long teamId);

        @HTTP(path = "stickers/messages/{messageId}", method = "DELETE", hasBody = true)
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deleteSticker(@Path("messageId") long messageId, @Query("teamId") long teamId);

    }
}
