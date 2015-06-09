package com.tosslab.jandi.app.ui.filedetail.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.network.client.sticker.StickerApiClient;
import com.tosslab.jandi.app.network.client.sticker.StickerApiClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.sticker.ReqSendSticker;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

/**
 * Created by Steve SeongUg Jung on 15. 6. 8..
 */
public class StickerCommentRequest implements Request<ResCommon> {

    private final Context context;
    private final int stickerGroupId;
    private final String stickerId;
    private final int teamId;
    private final int feedbackId;
    private final String message;

    private StickerCommentRequest(Context context, int stickerGroupId, String stickerId, int teamId, int feedbackId, String message) {
        this.context = context;
        this.stickerGroupId = stickerGroupId;
        this.stickerId = stickerId;
        this.teamId = teamId;
        this.feedbackId = feedbackId;
        this.message = message;
    }

    public static StickerCommentRequest create(Context context, int stickerGroupId, String stickerId, int teamId, int feedbackId, String message) {
        return new StickerCommentRequest(context, stickerGroupId, stickerId, teamId, feedbackId, message);
    }

    @Override
    public ResCommon request() throws JandiNetworkException {

        StickerApiClient stickerApiClient = new StickerApiClient_(context);
        stickerApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));

        ReqSendSticker reqSendSticker = getSendSticker(message);
        return stickerApiClient.sendStickerComment(reqSendSticker);
    }

    private ReqSendSticker getSendSticker(String message) {
        ReqSendSticker reqSendSticker;
        String type = null;
        String messageContent = null;
        if (!TextUtils.isEmpty(message)) {
            type = "";
            messageContent = message;
        }
        reqSendSticker = ReqSendSticker.create(stickerGroupId, stickerId, teamId, feedbackId, type, messageContent);
        return reqSendSticker;
    }
}
