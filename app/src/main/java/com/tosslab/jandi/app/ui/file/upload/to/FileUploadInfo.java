package com.tosslab.jandi.app.ui.file.upload.to;

/**
 * Created by Steve SeongUg Jung on 15. 6. 16..
 */
public class FileUploadInfo {
    private final String filePath;
    private final String fileName;
    private int entity;
    private String comment;

    public FileUploadInfo(String filePath, String fileName, int entity, String comment) {
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

    public int getEntity() {
        return entity;
    }

    public FileUploadInfo setEntity(int entity) {
        this.entity = entity;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public FileUploadInfo setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public static class Builder {
        private String filePath;
        private String fileName;
        private int entity;
        private String comment;

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder entity(int entity) {
            this.entity = entity;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public FileUploadInfo createFileUploadInfo() {
            return new FileUploadInfo(filePath, fileName, entity, comment);
        }
    }
}
