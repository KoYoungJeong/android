package com.tosslab.jandi.app.events.messages;

/**
 * Created by Steve SeongUg Jung on 15. 3. 19..
 */
public class ChatModeChangeEvent {
    private final boolean clicked;

    public ChatModeChangeEvent(boolean clicked) {
        this.clicked = clicked;
    }

    public boolean isClicked() {
        return clicked;
    }
}
