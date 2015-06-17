package com.tosslab.jandi.app.services.upload.to;

import com.tosslab.jandi.app.ui.file.upload.preview.to.FileUploadVO;

/**
 * Created by Steve SeongUg Jung on 15. 6. 17..
 */
public class FileUploadDTO {

    private String filePath;
    private String fileName;
    private int entity;
    private String comment;
    private UploadState uploadState = UploadState.IDLE;
    private int uploadProgress = 0;

    public FileUploadDTO(String filePath, String fileName, int entity, String comment) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.entity = entity;
        this.comment = comment;
    }

    public FileUploadDTO(FileUploadVO vo) {
        this(vo.getFilePath(), vo.getFileName(), vo.getEntity(), vo.getComment());
    }

    public int getUploadProgress() {
        return uploadProgress;
    }

    public void setUploadProgress(int uploadProgress) {
        this.uploadProgress = uploadProgress;
    }

    public String getFilePath() {

        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public int getEntity() {
        return entity;
    }

    public String getComment() {
        return comment;
    }

    public UploadState getUploadState() {
        return uploadState;
    }

    public void setUploadState(UploadState uploadState) {
        this.uploadState = uploadState;
    }

    public enum UploadState {
        IDLE, SUCCESS, PROGRESS, FAIL
    }

}
