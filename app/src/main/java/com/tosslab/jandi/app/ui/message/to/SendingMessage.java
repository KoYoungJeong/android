package com.tosslab.jandi.app.ui.message.to;

import com.tosslab.jandi.app.network.models.ReqMention;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 2. 4..
 */
public class SendingMessage {
    private final long localId;
    private final String message;
    //    int messageId;
    private StickerInfo stickerInfo;
    private List<ReqMention> mentions;

    public SendingMessage(long localId, ReqSendMessageV3 reqSendMessageV3) {
        this.localId = localId;
        this.message = reqSendMessageV3.getContent();
        this.mentions = reqSendMessageV3.getMentions();
    }

    public SendingMessage(long localId, String message, StickerInfo stickerInfo, List<ReqMention> mentions) {
        this.localId = localId;
        this.message = message;
        this.stickerInfo = stickerInfo;
        this.mentions = mentions;
    }

    public List<ReqMention> getMentions() {
        return mentions;
    }

    public long getLocalId() {
        return localId;
    }

//    public int getMessageId() {
//        return messageId;
//    }
//
//    public void setMessageId(int messageId) {
//        this.messageId = messageId;
//    }

    public String getMessage() {
        return message;
    }

    public StickerInfo getStickerInfo() {
        return stickerInfo;
    }

    public void setStickerInfo(StickerInfo stickerInfo) {
        this.stickerInfo = stickerInfo;
    }
}
