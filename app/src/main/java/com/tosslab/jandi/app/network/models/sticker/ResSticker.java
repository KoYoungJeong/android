package com.tosslab.jandi.app.network.models.sticker;

/**
 * Created by Steve SeongUg Jung on 15. 6. 3..
 */
public class ResSticker {
    private int id;
    private String mobile;
    private String web;
    private int groupId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getWeb() {
        return web;
    }

    public void setWeb(String web) {
        this.web = web;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
}
