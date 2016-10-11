package com.tosslab.jandi.app.events.messages;

/**
 * Created by tonyjs on 2016. 10. 11..
 */
public class LinkPreviewClickEvent {

    private String linkUrl;
    private TouchFrom touchFrom;

    public LinkPreviewClickEvent(String linkUrl, TouchFrom touchFrom) {
        this.linkUrl = linkUrl;
        this.touchFrom = touchFrom;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public TouchFrom getTouchFrom() {
        return touchFrom;
    }

    public enum TouchFrom {
        TEXT,
        IMAGE
    }
}
