package com.tosslab.jandi.app.ui.carousel.domain;

import android.os.Parcel;
import android.os.Parcelable;

import com.tosslab.jandi.app.network.models.ResMessages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Observable;

/**
 * Created by Bill MinWook Heo on 15. 6. 23..
 */
public class CarouselFileInfo implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CarouselFileInfo> CREATOR = new Parcelable.Creator<CarouselFileInfo>() {
        @Override
        public CarouselFileInfo createFromParcel(Parcel in) {
            return new CarouselFileInfo(in);
        }

        @Override
        public CarouselFileInfo[] newArray(int size) {
            return new CarouselFileInfo[size];
        }
    };
    private final long entityId;
    private final long fileMessageId;
    private final String fileName;
    private final String fileType;
    private final String ext;
    private final long size;
    private final String fileLinkUrl;
    private final String fileThumbUrl;
    private final String fileOriginalUrl;
    private final String fileCreateTime;
    private final String fileWriterName;
    private final long fileWriterId;
    private int fileCommentCount;
    private boolean isStarred;
    private boolean isExternalFileShared;
    private List<Long> sharedEntities;
    private String serverUrl;
    private String externalCode;

    private CarouselFileInfo(long entityId, long fileMessageId, String fileName, String fileType, String ext, long size, String serverUrl, String fileLinkUrl, String fileThumbUrl, String fileOriginalUrl, String fileCreateTime, long fileWriterId, String fileWriter, int fileCommentCount, boolean isStarred, String externalCode, boolean isExternalFileShared, List<Long> sharedEntities) {
        this.entityId = entityId;
        this.fileMessageId = fileMessageId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.ext = ext;
        this.size = size;
        this.serverUrl = serverUrl;
        this.fileLinkUrl = fileLinkUrl;
        this.fileThumbUrl = fileThumbUrl;
        this.fileOriginalUrl = fileOriginalUrl;
        this.fileCreateTime = fileCreateTime;
        this.fileWriterId = fileWriterId;
        this.fileWriterName = fileWriter;
        this.fileCommentCount = fileCommentCount;
        this.isStarred = isStarred;
        this.externalCode = externalCode;
        this.isExternalFileShared = isExternalFileShared;
        this.sharedEntities = sharedEntities;
    }

    protected CarouselFileInfo(Parcel in) {
        entityId = in.readLong();
        fileMessageId = in.readLong();
        fileName = in.readString();
        fileType = in.readString();
        ext = in.readString();
        size = in.readLong();
        fileLinkUrl = in.readString();
        fileThumbUrl = in.readString();
        fileOriginalUrl = in.readString();
        fileCreateTime = in.readString();
        fileWriterName = in.readString();
        fileWriterId = in.readLong();
        fileCommentCount = in.readInt();
        isStarred = in.readByte() != 0x00;
        isExternalFileShared = in.readByte() != 0x00;
        if (in.readByte() == 0x01) {
            sharedEntities = new ArrayList<Long>();
            in.readList(sharedEntities, Long.class.getClassLoader());
        } else {
            sharedEntities = null;
        }
        serverUrl = in.readString();
        externalCode = in.readString();
    }

    public String getFileWriterName() {
        return fileWriterName;
    }

    public String getFileCreateTime() {
        return fileCreateTime;
    }

    public String getExt() {
        return ext;
    }

    public long getSize() {
        return size;
    }

    public String getFileType() {
        return fileType;
    }

    public long getEntityId() {
        return entityId;
    }

    public long getFileMessageId() {
        return fileMessageId;
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

    public String getFileOriginalUrl() {
        return fileOriginalUrl;
    }

    public long getFileWriterId() {
        return fileWriterId;
    }

    public int getFileCommentCount() {
        return fileCommentCount;
    }

    public void setFileCommentCount(int fileCommentCount) {
        this.fileCommentCount = fileCommentCount;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public boolean isExternalFileShared() {
        return isExternalFileShared;
    }

    public List<Long> getSharedEntities() {
        return sharedEntities;
    }

    public void setSharedEntities(List<Long> sharedEntities) {
        this.sharedEntities = sharedEntities;
    }

    public void setIsStarred(boolean isStarred) {
        this.isStarred = isStarred;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getExternalCode() {
        return externalCode;
    }

    public void setExternalCode(String externalCode) {
        this.externalCode = externalCode;
    }

    public void setIsExternalFileShared(boolean isExternalFileShared) {
        this.isExternalFileShared = isExternalFileShared;
    }

    @Override
    public String toString() {
        return "CarouselFileInfo{" +
                "entityId=" + entityId +
                ", fileMessageId=" + fileMessageId +
                ", fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", ext='" + ext + '\'' +
                ", size=" + size +
                ", fileLinkUrl='" + fileLinkUrl + '\'' +
                ", fileThumbUrl='" + fileThumbUrl + '\'' +
                ", fileOriginalUrl='" + fileOriginalUrl + '\'' +
                ", fileCreateTime='" + fileCreateTime + '\'' +
                ", fileWriterName='" + fileWriterName + '\'' +
                ", fileWriterId=" + fileWriterId +
                ", fileCommentCount=" + fileCommentCount +
                ", isStarred=" + isStarred +
                ", isExternalFileShared=" + isExternalFileShared +
                ", sharedEntities=" + sharedEntities +
                ", serverUrl='" + serverUrl + '\'' +
                ", externalCode='" + externalCode + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(entityId);
        dest.writeLong(fileMessageId);
        dest.writeString(fileName);
        dest.writeString(fileType);
        dest.writeString(ext);
        dest.writeLong(size);
        dest.writeString(fileLinkUrl);
        dest.writeString(fileThumbUrl);
        dest.writeString(fileOriginalUrl);
        dest.writeString(fileCreateTime);
        dest.writeString(fileWriterName);
        dest.writeLong(fileWriterId);
        dest.writeInt(fileCommentCount);
        dest.writeByte((byte) (isStarred ? 0x01 : 0x00));
        dest.writeByte((byte) (isExternalFileShared ? 0x01 : 0x00));
        if (sharedEntities == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(sharedEntities);
        }
        dest.writeString(serverUrl);
        dest.writeString(externalCode);
    }

    public static class Builder {
        private long entityId;
        private long fileMessageId;
        private long fileWriterId;
        private String fileName;
        private String fileLinkUrl;
        private String fileType;
        private String ext;
        private long size;
        private String fileCreateTime;
        private String fileWriterName;
        private String fileThumbUrl;
        private String fileOriginalUrl;
        private int fileCommentCount;
        private boolean isStarred;
        private boolean isExternalShared;
        private List<Long> sharedEntities;
        private String serverUrl;
        private String externalCode;

        public Builder fileWriterName(String fileWriter) {
            this.fileWriterName = fileWriter;
            return this;
        }

        public Builder ext(String ext) {
            this.ext = ext;
            return this;
        }

        public Builder size(long size) {
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

        public Builder entityId(long entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder serverUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        public Builder fileMessageId(long fileMessageId) {
            this.fileMessageId = fileMessageId;
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

        public Builder fileThumbUrl(String fileThumbUrl) {
            this.fileThumbUrl = fileThumbUrl;
            return this;
        }

        public Builder fileOriginalUrl(String fileOriginalUrl) {
            this.fileOriginalUrl = fileOriginalUrl;
            return this;
        }

        public Builder fileCommentCount(int fileCommentCount) {
            this.fileCommentCount = fileCommentCount;
            return this;
        }

        public Builder isStarred(boolean isStarred) {
            this.isStarred = isStarred;
            return this;
        }

        public Builder fileWriterId(long writerId) {
            this.fileWriterId = writerId;
            return this;
        }

        public Builder externalCode(String externalCode) {
            this.externalCode = externalCode;
            return this;
        }

        public Builder isExternalShared(boolean isExternalShared) {
            this.isExternalShared = isExternalShared;
            return this;
        }

        public Builder sharedEntities(Collection<ResMessages.OriginalMessage.IntegerWrapper> sharedEntities) {
            List<Long> sharedEntitiesIds = new ArrayList<>();
            Observable.from(sharedEntities)
                    .map(ResMessages.OriginalMessage.IntegerWrapper::getShareEntity)
                    .collect(() -> sharedEntitiesIds, List::add)
                    .subscribe();
            this.sharedEntities = sharedEntitiesIds;
            return this;
        }

        public CarouselFileInfo create() {
            return new CarouselFileInfo(entityId, fileMessageId, fileName, fileType, ext, size, serverUrl, fileLinkUrl, fileThumbUrl, fileOriginalUrl, fileCreateTime, fileWriterId, fileWriterName, fileCommentCount, isStarred, externalCode, isExternalShared, sharedEntities);
        }
    }
}
