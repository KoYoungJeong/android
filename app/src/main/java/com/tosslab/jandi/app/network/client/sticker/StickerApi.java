package com.tosslab.jandi.app.network.client.sticker;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class StickerApi extends ApiTemplate<StickerApi.Api> {
    public StickerApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public List<ResMessages.Link> sendStickerComment(ReqSendSticker reqSendSticker) throws RetrofitException {
        return call(() -> getApi().sendStickerComment(reqSendSticker));
    }

    interface Api {

        @POST("stickers/comment")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<List<ResMessages.Link>> sendStickerComment(@Body ReqSendSticker reqSendSticker);

    }
}
