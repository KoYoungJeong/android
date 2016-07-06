package com.tosslab.jandi.app.ui.poll.detail.dto;

import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.poll.Poll;

import java.util.List;

/**
 * Created by tonyjs on 16. 6. 27..
 */
public class PollDetail {
    private long pollId;
    private Poll poll;
    private List<ResMessages.OriginalMessage> pollComments;

    public PollDetail(long pollId) {
        this.pollId = pollId;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public List<ResMessages.OriginalMessage> getPollComments() {
        return pollComments;
    }

    public void setPollComments(List<ResMessages.OriginalMessage> pollComments) {
        this.pollComments = pollComments;
    }

    public long getPollId() {
        return pollId;
    }

    @Override
    public String toString() {
        return "PollDetail{" +
                "poll=" + poll +
                ", pollComments=" + pollComments +
                '}';
    }
}
