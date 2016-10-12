package com.tosslab.jandi.app.network.models;

import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.util.List;

/**
 * Created by tee on 2016. 10. 12..
 */

public class ReqSendMessages {

    private String stickerId;
    private long groupId;
    private String text;
    private List<MentionObject> mentions;

    public String getStickerId() {
        return stickerId;
    }

    public void setStickerId(String stickerId) {
        this.stickerId = stickerId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<MentionObject> getMentions() {
        return mentions;
    }

    public void setMentions(List<MentionObject> mentions) {
        this.mentions = mentions;
    }
}
