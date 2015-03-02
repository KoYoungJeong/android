package com.tosslab.jandi.app.ui.entities.chats.to;

/**
 * Created by Steve SeongUg Jung on 15. 3. 2..
 */
public class DisableDummyItem extends ChatChooseItem {

    private boolean isExpand;
    private int disabledCount;

    public DisableDummyItem(boolean isExpand, int disabledCount) {
        this.isExpand = isExpand;
        this.disabledCount = disabledCount;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean isExpand) {
        this.isExpand = isExpand;
    }

    public int getDisabledCount() {
        return disabledCount;
    }

    public void setDisabledCount(int disabledCount) {
        this.disabledCount = disabledCount;
    }
}
