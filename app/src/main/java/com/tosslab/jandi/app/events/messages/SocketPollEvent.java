package com.tosslab.jandi.app.events.messages;

import com.tosslab.jandi.app.network.models.poll.Poll;

/**
 * Created by tonyjs on 16. 6. 21..
 */
public class SocketPollEvent {
    private Poll poll;
    private Type type;

    public SocketPollEvent(Poll poll, Type type) {
        this.poll = poll;
        this.type = type;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        CREATED, FINISHED, DELETED
    }
}
