package com.tosslab.jandi.app.events.search;

/**
 * Created by Steve SeongUg Jung on 15. 3. 13..
 */
public class SearchResultScrollEvent {
    private final int offset;

    public SearchResultScrollEvent(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }
}
