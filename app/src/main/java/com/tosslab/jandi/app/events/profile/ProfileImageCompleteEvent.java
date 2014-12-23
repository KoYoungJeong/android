package com.tosslab.jandi.app.events.profile;

import java.io.File;

/**
 * Created by Steve SeongUg Jung on 14. 12. 23..
 */
public class ProfileImageCompleteEvent {
    private final Exception exception;
    private final File filePath;

    public ProfileImageCompleteEvent(Exception exception, File filePath) {
        this.exception = exception;
        this.filePath = filePath;
    }

    public Exception getException() {
        return exception;
    }

    public File getFilePath() {
        return filePath;
    }
}
