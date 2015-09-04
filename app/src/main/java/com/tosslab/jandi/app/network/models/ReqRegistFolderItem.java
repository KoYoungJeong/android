package com.tosslab.jandi.app.network.models;

/**
 * Created by tee on 15. 8. 25..
 */
public class ReqRegistFolderItem {

    private int itemId;

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    @Override
    public String toString() {
        return "ReqRegistFolderItem{" +
                "itemId=" + itemId +
                '}';
    }
}
