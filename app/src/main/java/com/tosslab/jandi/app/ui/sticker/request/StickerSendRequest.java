package com.tosslab.jandi.app.ui.sticker.request;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.client.sticker.StickerApiClient;
import com.tosslab.jandi.app.network.client.sticker.StickerApiClient_;
import com.tosslab.jandi.app.network.manager.Request;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.sticker.SendSticker;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

/**
 * Created by Steve SeongUg Jung on 15. 6. 8..
 */
public class StickerSendRequest implements Request<ResCommon> {

    private Context context;

    private String stickerId;
    private int stickerGroupId;
    private int teamId;
    private int share;
    private String message;

    private StickerSendRequest(Context context, String stickerId, int stickerGroupId, int teamId, int share, String message) {
        this.context = context;
        this.stickerId = stickerId;
        this.stickerGroupId = stickerGroupId;
        this.teamId = teamId;
        this.share = share;
        this.message = message;
    }

    public static StickerSendRequest create(Context context, String stickerId, int stickerGroupId, int teamId, int share, String message) {
        return new StickerSendRequest(context, stickerId, stickerGroupId, teamId, share, message);
    }

    @Override
    public ResCommon request() throws JandiNetworkException {

        StickerApiClient stickerApiClient = new StickerApiClient_(context);
        stickerApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));

        SendSticker sendSticker = getSendSticker();
        return stickerApiClient.sendSticker(sendSticker);
    }

    private SendSticker getSendSticker() {

        FormattedEntity entity = EntityManager.getInstance(context).getEntityById(share);

        // FIXME
        String type = entity.isPublicTopic() ? "channels" : entity.isPrivateGroup() ? "privateGroups" : "users";

        if (!TextUtils.isEmpty(message)) {
            return SendSticker.create(stickerId, stickerGroupId, teamId, share, type, message);
        } else {
            return SendSticker.create(stickerId, stickerGroupId, teamId, share, type, "");
        }

    }
}
