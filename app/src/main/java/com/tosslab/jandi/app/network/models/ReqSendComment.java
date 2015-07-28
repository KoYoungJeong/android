package com.tosslab.jandi.app.network.models;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 6. 25..
 */
public class ReqSendComment {

    public String comment;

    public List<ReqMention> mentions;

    public ReqSendComment(String comment, List<ReqMention> mentions) {
        this.comment = comment;
        this.mentions = mentions;
    }

    public ReqSendComment() {
    }

    public String getComment() {
        return comment;
    }

    public List<ReqMention> getMentions() {
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
