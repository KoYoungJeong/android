package com.tosslab.jandi.app.events.files;

import com.tosslab.jandi.app.network.models.ResMessages;

/**
 * Created by tonyjs on 16. 7. 5..
 */
public class RequestShowCarouselViewerEvent {
    private long fileMessageId;
    private ResMessages.FileContent content;
    private boolean shouldOpenImmediately;

    public RequestShowCarouselViewerEvent(long fileMessageId, ResMessages.FileContent content, boolean shouldOpenImmediately) {
        this.fileMessageId = fileMessageId;
        this.content = content;
        this.shouldOpenImmediately = shouldOpenImmediately;
    }

    public long getFileMessageId() {
        return fileMessageId;
    }

    public ResMessages.FileContent getContent() {
        return content;
    }

    public boolean shouldOpenImmediately() {
        return shouldOpenImmediately;
    }
}
