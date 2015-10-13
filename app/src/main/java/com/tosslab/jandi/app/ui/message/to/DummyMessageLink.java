package com.tosslab.jandi.app.ui.message.to;

import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 2. 4..
 */
public class DummyMessageLink extends ResMessages.Link {

    private final long localId;
    private String status;
    private List<MentionObject> mentions;

    public DummyMessageLink(long localId, String message, String status, List<MentionObject> mentions) {
        this.localId = localId;

        ResMessages.TextMessage textMessage = new ResMessages.TextMessage();
        textMessage.content = new ResMessages.TextContent();
        textMessage.content.body = message;
        textMessage.createTime = new Date(System.currentTimeMillis());
        textMessage.updateTime = new Date(System.currentTimeMillis());
        textMessage.mentions = mentions;

        this.time = new Date(System.currentTimeMillis());
        this.message = textMessage;
        this.status = status;
        this.mentions = mentions;
    }

    public DummyMessageLink(long localId, String status, int stickerGroupId, String stickerId) {
        this.localId = localId;

        ResMessages.StickerMessage textMessage = new ResMessages.StickerMessage();
        textMessage.content = new ResMessages.StickerContent();
        textMessage.createTime = new Date(System.currentTimeMillis());
        textMessage.updateTime = new Date(System.currentTimeMillis());

        textMessage.content.groupId = stickerGroupId;
        textMessage.content.stickerId = stickerId;

        this.time = new Date(System.currentTimeMillis());
        this.message = textMessage;
        this.status = status;
        this.mentions = new ArrayList<>();
    }

    public long getLocalId() {
        return localId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<MentionObject> getMentions() {
        return mentions;
    }
}
