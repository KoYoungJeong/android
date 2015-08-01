package com.tosslab.jandi.app.network.models;

import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 6. 25..
 */
public class ReqSendComment {

    public String comment;

    public List<MentionObject> mentions;

    public ReqSendComment(String comment, List<MentionObject> mentions) {
        this.comment = comment;
        this.mentions = mentions;
    }

    public ReqSendComment() {
    }

    public String getComment() {
        return comment;
    }

    public List<MentionObject> getMentions() {
        return mentions;
    }

    @Override
    public String toString() {
        return "ReqSendComment{" +
                "comment='" + comment + '\'' +
                ", mentions=" + mentions.toString() +
                '}';
    }
}
