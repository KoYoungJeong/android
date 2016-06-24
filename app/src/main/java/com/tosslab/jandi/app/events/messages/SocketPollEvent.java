package com.tosslab.jandi.app.events.messages;

import com.tosslab.jandi.app.network.models.poll.Poll;

/**
 * Created by tonyjs on 16. 6. 21..
 */
public class SocketPollEvent {
    private Poll poll;

    public SocketPollEvent(Poll poll) {
        this.poll = poll;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }
}
