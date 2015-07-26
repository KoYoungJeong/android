package com.tosslab.jandi.app.events.messages;

/**
 * Created by tee on 15. 7. 22..
 */
public class SelectedMemberInfoForMensionEvent {

    private String name;
    private int id;
    private String type;

    public SelectedMemberInfoForMensionEvent(String name, int id, String type) {
        this.name = name;
        this.id = id;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
