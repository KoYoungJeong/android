package com.tosslab.jandi.app.events.poll;

import com.tosslab.jandi.app.network.models.poll.Poll;

/**
 * Created by tonyjs on 16. 6. 27..
 */
public class RequestShowPollParticipantsEvent {

    public enum Type {
        ALL, OPTION
    }

    private Poll poll;
    private Type type = Type.ALL;
    private Poll.Item option;

    private RequestShowPollParticipantsEvent(Poll poll) {
        this.poll = poll;
        this.type = Type.ALL;
    }

    private RequestShowPollParticipantsEvent(Poll poll, Poll.Item item) {
        this.poll = poll;
        this.type = Type.OPTION;
        this.option = item;
    }

    public static RequestShowPollParticipantsEvent option(Poll poll, Poll.Item item) {
        return new RequestShowPollParticipantsEvent(poll, item);
    }

    public static RequestShowPollParticipantsEvent all(Poll poll) {
        return new RequestShowPollParticipantsEvent(poll);
    }

    public Poll getPoll() {
        return poll;
    }

    public Type getType() {
        return type;
    }

    public Poll.Item getOption() {
        return option;
    }
}
