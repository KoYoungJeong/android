package com.tosslab.jandi.app.events.search;

/**
 * Created by Steve SeongUg Jung on 15. 3. 11..
 */
public class SearchRequestEvent {
    private final String query;

    public SearchRequestEvent(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
