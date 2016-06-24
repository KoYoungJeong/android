package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.poll.*;
/**
 * Created by tonyjs on 16. 6. 14..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResPollDetail {

    private Poll poll;
    private Message message;

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    @Override
    public String toString() {
        return "ResPollDetail{" +
                "poll=" + poll +
                ", message=" + message +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Message {
        private long id;
        private boolean isStarred;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public boolean isStarred() {
            return isStarred;
        }

        public void setStarred(boolean starred) {
            isStarred = starred;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "id=" + id +
                    ", isStarred=" + isStarred +
                    '}';
        }
    }

}
