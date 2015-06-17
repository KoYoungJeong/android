package com.tosslab.jandi.app.ui.file.upload.preview.to;

/**
 * Created by Steve SeongUg Jung on 15. 6. 16..
 */
public class FileUploadVO {
    private final String filePath;
    private final String fileName;
    private int entity;
    private String comment;

    public FileUploadVO(String filePath, String fileName, int entity, String comment) {
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

    public FileUploadVO setEntity(int entity) {
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

        public FileUploadVO createFileUploadInfo() {
            return new FileUploadVO(filePath, fileName, entity, comment);
        }
    }
}
