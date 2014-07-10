package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 7. 10..
 */
public class ConfirmJoinChannelEvent {
    public int channelId;
    public ConfirmJoinChannelEvent(int channelId) {
        this.channelId = channelId;
    }
}
