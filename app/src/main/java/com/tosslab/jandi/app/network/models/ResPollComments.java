package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * Created by tonyjs on 16. 6. 23..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResPollComments {
    private int commentCount;
    private List<ResMessages.OriginalMessage> comments;

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public List<ResMessages.OriginalMessage> getComments() {
        return comments;
    }

    public void setComments(List<ResMessages.OriginalMessage> comments) {
        this.comments = comments;
    }

    public static ResPollComments empty() {
        return new ResPollComments();
    }

    @Override
    public String toString() {
        return "ResPollComments{" +
                "commentCount=" + commentCount +
                ", comments=" + comments +
                '}';
    }
}
