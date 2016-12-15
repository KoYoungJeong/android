package com.tosslab.jandi.app.events.files;


public class GifReadyEvent {
    private final String originalUrl;

    public GifReadyEvent(String originalUrl) {this.originalUrl = originalUrl;}

    public String getOriginalUrl() {
        return originalUrl;
    }
}
