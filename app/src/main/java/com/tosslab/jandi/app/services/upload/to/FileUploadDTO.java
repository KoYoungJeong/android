package com.tosslab.jandi.app.services.upload.to;

import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.ui.file.upload.preview.to.FileUploadVO;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 6. 17..
 */
public class FileUploadDTO {

    private String filePath;
    private String fileName;
    private long teamId;
    private long entity;
    private String comment;
    private List<MentionObject> mentions;
    private UploadState uploadState = UploadState.IDLE;
    private int uploadProgress = 0;

    public FileUploadDTO() {
    }

    public FileUploadDTO(String filePath, String fileName, long entity, String comment) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.entity = entity;
        this.comment = comment;
    }

    public FileUploadDTO(FileUploadVO vo) {
        this(vo.getFilePath(), vo.getFileName(), vo.getEntity(), vo.getComment());
    }

    public List<MentionObject> getMentions() {
        return mentions;
    }

    public FileUploadDTO setMentions(List<MentionObject> mentions) {
        this.mentions = mentions;
        return this;
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

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getEntity() {
        return entity;
    }

    public void setEntity(long entity) {
        this.entity = entity;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public UploadState getUploadState() {
        return uploadState;
    }

    public void setUploadState(UploadState uploadState) {
        this.uploadState = uploadState;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public enum UploadState {
        IDLE, SUCCESS, PROGRESS, FAIL
    }

}
