package com.tosslab.jandi.app.ui.message.to.queue;

import com.tosslab.jandi.app.services.socket.to.SocketRoomMarkerEvent;

/**
 * Created by Steve SeongUg Jung on 15. 4. 14..
 */
public class MarkerUpdateMessageQueue implements MessageQueue {
    private final SocketRoomMarkerEvent.Marker marker;

    public MarkerUpdateMessageQueue(SocketRoomMarkerEvent.Marker marker) {
        this.marker = marker;
    }

    @Override
    public LoadType getQueueType() {
        return LoadType.Marker;
    }

    @Override
    public Object getData() {
        return marker;
    }
}
