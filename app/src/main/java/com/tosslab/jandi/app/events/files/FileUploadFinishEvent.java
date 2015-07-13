package com.tosslab.jandi.app.events.files;

import com.tosslab.jandi.app.services.upload.to.FileUploadDTO;

/**
 * Created by Steve SeongUg Jung on 15. 6. 17..
 */
public class FileUploadFinishEvent {

    private final FileUploadDTO fileUploadDTO;

    public FileUploadFinishEvent(FileUploadDTO fileUploadDTO) {
        this.fileUploadDTO = fileUploadDTO;
    }

    public FileUploadDTO getFileUploadDTO() {
        return fileUploadDTO;
    }
}
