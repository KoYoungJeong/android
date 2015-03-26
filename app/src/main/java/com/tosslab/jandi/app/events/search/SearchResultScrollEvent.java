package com.tosslab.jandi.app.events.search;

/**
 * Created by Steve SeongUg Jung on 15. 3. 13..
 */
public class SearchResultScrollEvent {
    private final Class from;
    private final int offset;

    public SearchResultScrollEvent(Class from, int offset) {
        this.from = from;
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public Class getFrom() {
        return from;
    }
}
