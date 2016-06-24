package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.poll.Poll;

/**
 * Created by tonyjs on 16. 6. 14..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResCreatePoll {
    private Poll poll;
    private ResMessages.Link linkMessage;
    private ResMessages.Link linkComment;

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public ResMessages.Link getLinkMessage() {
        return linkMessage;
    }

    public void setLinkMessage(ResMessages.Link linkMessage) {
        this.linkMessage = linkMessage;
    }

    public ResMessages.Link getLinkComment() {
        return linkComment;
    }

    public void setLinkComment(ResMessages.Link linkComment) {
        this.linkComment = linkComment;
    }
}
