package com.tosslab.jandi.app.ui.carousel.domain;

/**
 * Created by Bill MinWook Heo on 15. 6. 23..
 */
public class CarouselFileInfo {

    private final int entityId;
    private final int fileLinkId;
    private final String fileName;
    private final String fileType;
    private final String ext;
    private final int size;
    private final String fileLinkUrl;
    private final String fileThumbUrl;
    private final String fileCreateTime;
    private final String fileWriter;

    private CarouselFileInfo(int entityId, int fileLinkId, String fileName, String fileType, String ext, int size, String fileLinkUrl, String fileThumbUrl, String fileCreateTime, String fileWriter) {
        this.entityId = entityId;
        this.fileLinkId = fileLinkId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.ext = ext;
        this.size = size;
        this.fileLinkUrl = fileLinkUrl;
        this.fileThumbUrl = fileThumbUrl;
        this.fileCreateTime = fileCreateTime;
        this.fileWriter = fileWriter;
    }

    public String getFileWriter() {
        return fileWriter;
    }

    public String getFileCreateTime() {
        return fileCreateTime;
    }

    public String getExt() {
        return ext;
    }

    public int getSize() {
        return size;
    }

    public String getFileType() {
        return fileType;
    }

    public int getEntityId() {
        return entityId;
    }

    public int getFileLinkId() {
        return fileLinkId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileLinkUrl() {
        return fileLinkUrl;
    }

    public String getFileThumbUrl() {
        return fileThumbUrl;
    }

    public static class Builder {
        private int entityId;
        private int fileLinkId;
        private String fileName;
        private String fileLinkUrl;
        private String fileType;
        private String ext;
        private int size;
        private String fileCreateTime;
        private String fileWriter;
        private String fileThumbUrl;

        public Builder fileWriter(String fileWriter) {
            this.fileWriter = fileWriter;
            return this;
        }

        public Builder ext(String ext) {
            this.ext = ext;
            return this;
        }

        public Builder size(int size) {
            this.size = size;
            return this;
        }

        public Builder fileCreateTime(String fileCreateTime) {
            this.fileCreateTime = fileCreateTime;
            return this;
        }

        public Builder fileType(String fileType) {
            this.fileType = fileType;
            return this;

        }

        public Builder entityId(int entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder fileLinkId(int fileLinkId) {
            this.fileLinkId = fileLinkId;
            return this;
        }

        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder fileLinkUrl(String fileLinkUrl) {
            this.fileLinkUrl = fileLinkUrl;
            return this;
        }

        public CarouselFileInfo create() {
            return new CarouselFileInfo(entityId, fileLinkId, fileName, fileType, ext, size, fileLinkUrl, fileThumbUrl, fileCreateTime, fileWriter);
        }

        public Builder fileThumbUrl(String fileThumbUrl) {
            this.fileThumbUrl = fileThumbUrl;
            return this;
        }
    }
}
