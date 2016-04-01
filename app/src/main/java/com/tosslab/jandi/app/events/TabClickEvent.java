package com.tosslab.jandi.app.events;

/**
 * Created by tonyjs on 16. 3. 29..
 */
public class TabClickEvent {
    private int index;

    public TabClickEvent(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
