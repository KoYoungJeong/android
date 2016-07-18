package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.poll.Poll;

/**
 * Created by tonyjs on 16. 6. 24..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResPollCommentCreated {
    private Poll poll;
    private ResMessages.Link linkSticker;
    private ResMessages.Link linkComment;

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public ResMessages.Link getLinkSticker() {
        return linkSticker;
    }

    public void setLinkSticker(ResMessages.Link linkSticker) {
        this.linkSticker = linkSticker;
    }

    public ResMessages.Link getLinkComment() {
        return linkComment;
    }

    public void setLinkComment(ResMessages.Link linkComment) {
        this.linkComment = linkComment;
    }

    @Override
    public String toString() {
        return "ResPollCommentCreated{" +
                "poll=" + poll +
                ", linkSticker=" + linkSticker +
                ", linkComment=" + linkComment +
                '}';
    }
}
