package com.tosslab.jandi.app.network.models.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ReqStickerMessage implements ReqMessage {
    private String stickerId;
    @JsonProperty("groupId")
    private long stickerGroupId;

    public ReqStickerMessage() { }

    public ReqStickerMessage(String stickerId, long stickerGroupId) {
        this.stickerId = stickerId;
        this.stickerGroupId = stickerGroupId;
    }

    public String getStickerId() {
        return stickerId;
    }

    public void setStickerId(String stickerId) {
        this.stickerId = stickerId;
    }

    public long getStickerGroupId() {
        return stickerGroupId;
    }

    public void setStickerGroupId(long stickerGroupId) {
        this.stickerGroupId = stickerGroupId;
    }
}
