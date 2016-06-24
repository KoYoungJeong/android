package com.tosslab.jandi.app.events;

import java.util.Collection;

/**
 * Created by tonyjs on 16. 6. 23..
 */
public class RequestVotePollEvent {

    private long pollId;
    private Collection<Integer> seqs;

    private RequestVotePollEvent(long pollId, Collection<Integer> seqs) {
        this.pollId = pollId;
        this.seqs = seqs;
    }

    public static RequestVotePollEvent create(long pollId, Collection<Integer> seqs) {
        return new RequestVotePollEvent(pollId, seqs);
    }

    public long getPollId() {
        return pollId;
    }

    public Collection<Integer> getSeqs() {
        return seqs;
    }
}
