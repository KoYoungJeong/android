package com.tosslab.jandi.app.events.search;

/**
 * Created by Steve SeongUg Jung on 15. 3. 11..
 */
public class NewSearchRequestEvent {
    private final String query;

    public NewSearchRequestEvent(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
