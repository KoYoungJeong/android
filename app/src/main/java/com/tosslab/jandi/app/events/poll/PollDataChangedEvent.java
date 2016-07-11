package com.tosslab.jandi.app.events.poll;

import com.tosslab.jandi.app.network.models.poll.Poll;

/**
 * Created by tonyjs on 16. 6. 23..
 */
public class PollDataChangedEvent {

    private Poll poll;

    private PollDataChangedEvent(Poll poll) {
        this.poll = poll;
    }

    public static PollDataChangedEvent create(Poll poll) {
        return new PollDataChangedEvent(poll);
    }

    public Poll getPoll() {
        return poll;
    }
}
