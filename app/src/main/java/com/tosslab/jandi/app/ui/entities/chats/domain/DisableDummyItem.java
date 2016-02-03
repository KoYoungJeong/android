package com.tosslab.jandi.app.ui.entities.chats.domain;

/**
 * Created by Steve SeongUg Jung on 15. 3. 2..
 */
public class DisableDummyItem extends ChatChooseItem {

    private int disabledCount;

    public DisableDummyItem(int disabledCount) {
        this.disabledCount = disabledCount;
    }

    public int getDisabledCount() {
        return disabledCount;
    }
}
