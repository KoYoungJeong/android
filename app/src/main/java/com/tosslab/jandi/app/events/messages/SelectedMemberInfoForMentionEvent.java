package com.tosslab.jandi.app.events.messages;

/**
 * Created by tee on 15. 7. 22..
 */
public class SelectedMemberInfoForMentionEvent {

    private String name;
    private long id;
    private String type;

    public SelectedMemberInfoForMentionEvent(String name, long id, String type) {
        this.name = name;
        this.id = id;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
