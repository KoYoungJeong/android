package com.tosslab.jandi.app.ui.file.upload.preview.to;

import java.io.Serializable;

/**
 * Created by Steve SeongUg Jung on 15. 6. 16..
 */
public class FileUploadVO implements Serializable {
    public static final long serialVersionUID = -20150827;

    private final String filePath;
    private String fileName;
    private long entity;
    private String comment;

    public FileUploadVO(String filePath, String fileName, long entity, String comment) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.entity = entity;
        this.comment = comment;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String name) {
        this.fileName = name;
    }

    public long getEntity() {
        return entity;
    }

    public FileUploadVO setEntity(long entity) {
        this.entity = entity;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public FileUploadVO setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public static class Builder {
        private String filePath;
        private String fileName;
        private long entity;
        private String comment;

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder entity(long entity) {
            this.entity = entity;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public FileUploadVO createFileUploadInfo() {
            return new FileUploadVO(filePath, fileName, entity, comment);
        }
    }
}
