package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.util.List;

/**
 * Created by tonyjs on 16. 6. 24..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ReqSendPollComment {

    private String stickerId;
    private long groupId;
    private String comment;
    private List<MentionObject> mentions;

    public ReqSendPollComment(String comment, List<MentionObject> mentions) {
        this.comment = comment;
        this.mentions = mentions;
    }

    public ReqSendPollComment(long groupId, String stickerId, String comment, List<MentionObject> mentions) {
        this.groupId = groupId;
        this.stickerId = stickerId;
        this.comment = comment;
        this.mentions = mentions;
    }

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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<MentionObject> getMentions() {
        return mentions;
    }

    public void setMentions(List<MentionObject> mentions) {
        this.mentions = mentions;
    }
}
