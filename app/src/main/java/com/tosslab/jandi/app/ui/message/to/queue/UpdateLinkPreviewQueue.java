package com.tosslab.jandi.app.ui.message.to.queue;

import com.tosslab.jandi.app.services.socket.to.SocketLinkPreviewThumbnailEvent;

/**
 * Created by tonyjs on 15. 9. 15..
 */
public class UpdateLinkPreviewQueue implements MessageQueue {

    private SocketLinkPreviewThumbnailEvent event;

    public UpdateLinkPreviewQueue(SocketLinkPreviewThumbnailEvent event) {
        this.event = event;
    }

    @Override
    public LoadType getQueueType() {
        return LoadType.UpdateLinkPreview;
    }

    @Override
    public SocketLinkPreviewThumbnailEvent getData() {
        return event;
    }
}
