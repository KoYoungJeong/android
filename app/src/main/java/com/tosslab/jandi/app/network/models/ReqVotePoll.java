package com.tosslab.jandi.app.network.models;

import java.util.Collection;

/**
 * Created by tonyjs on 16. 6. 23..
 */
public class ReqVotePoll {
    private Collection<Integer> itemSeqs;

    private ReqVotePoll(Collection<Integer> itemSeqs) {
        this.itemSeqs = itemSeqs;
    }

    public static ReqVotePoll create(Collection<Integer> itemSeqs) {
        return new ReqVotePoll(itemSeqs);
    }

    public Collection<Integer> getItemSeqs() {
        return itemSeqs;
    }

    public void setItemSeqs(Collection<Integer> itemSeqs) {
        this.itemSeqs = itemSeqs;
    }
}
