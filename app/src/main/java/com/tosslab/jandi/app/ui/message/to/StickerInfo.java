package com.tosslab.jandi.app.ui.message.to;

/**
 * Created by Steve SeongUg Jung on 15. 6. 5..
 */
public class StickerInfo {
    private String stickerId;
    private int stickerGroupId;

    public StickerInfo() {
    }

    public StickerInfo(StickerInfo stickerInfo) {
        this.stickerGroupId = stickerInfo.getStickerGroupId();
        this.stickerId = stickerInfo.getStickerId();
    }

    public String getStickerId() {
        return stickerId;
    }

    public void setStickerId(String stickerId) {
        this.stickerId = stickerId;
    }

    public int getStickerGroupId() {
        return stickerGroupId;
    }

    public void setStickerGroupId(int stickerGroupId) {
        this.stickerGroupId = stickerGroupId;
    }
}
