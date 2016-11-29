package com.tosslab.jandi.app.network.models.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by tee on 16. 7. 20..
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResSearch {

    // 추가 페이지 존재 여부
    private boolean hasMore;

    // 현재 페이지에서 검색 결과 수
    private int recordCount;

    // 전체 검색 결과 수
    private int totalCount;

    // 현재 페이지
    private int page;

    // 검색 키워드가 검색 엔진에 의해 토큰으로 분리된 결과
    private String[] tokens;

    private List<SearchRecord> records;

    public boolean hasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String[] getTokens() {
        return tokens;
    }

    public void setTokens(String[] tokens) {
        this.tokens = tokens;
    }

    public List<SearchRecord> getRecords() {
        return records;
    }

    public void setRecords(List<SearchRecord> records) {
        this.records = records;
    }

    @Override
    public String toString() {
        return "ResSearch{" +
                "hasMore=" + hasMore +
                ", recordCount=" + recordCount +
                ", totalCount=" + totalCount +
                ", page=" + page +
                ", tokens=" + Arrays.toString(tokens) +
                ", records=" + records +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class SearchRecord {
        private long roomId;
        private long linkId;
        private long messageId;
        private long writerId;
        private String contentType;
        private String text;
        private Date createdAt;
        private String feedbackType;
        private List<MentionObject> mentions;
        private File file;
        private Poll poll;

        public long getRoomId() {
            return roomId;
        }

        public void setRoomId(long roomId) {
            this.roomId = roomId;
        }

        public long getLinkId() {
            return linkId;
        }

        public void setLinkId(long linkId) {
            this.linkId = linkId;
        }

        public long getMessageId() {
            return messageId;
        }

        public void setMessageId(long messageId) {
            this.messageId = messageId;
        }

        public long getWriterId() {
            return writerId;
        }

        public void setWriterId(long writerId) {
            this.writerId = writerId;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getFeedbackType() {
            return feedbackType;
        }

        public void setFeedbackType(String feedbackType) {
            this.feedbackType = feedbackType;
        }

        public List<MentionObject> getMentions() {
            return mentions;
        }

        public void setMentions(List<MentionObject> mentions) {
            this.mentions = mentions;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public Poll getPoll() {
            return poll;
        }

        public void setPoll(Poll poll) {
            this.poll = poll;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class File {
        private long id;
        private long writerId;
        private String title;
        private long commentCount;
        private long roomId;
        private long size;
        private String serverUrl;
        private String fileUrl;
        private String icon;
        private String ext;
        private int sharedCount;
        private String thumbnailUrl;
        private String smallThumbnailUrl;
        private String mediumThumbnailUrl;
        private String largeThumbnailUrl;

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }

        public String getServerUrl() {
            return serverUrl;
        }

        public void setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getWriterId() {
            return writerId;
        }

        public void setWriterId(long writerId) {
            this.writerId = writerId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public long getCommentCount() {
            return commentCount;
        }

        public void setCommentCount(long commentCount) {
            this.commentCount = commentCount;
        }

        public long getRoomId() {
            return roomId;
        }

        public void setRoomId(long roomId) {
            this.roomId = roomId;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public int getSharedCount() {
            return sharedCount;
        }

        public void setSharedCount(int sharedCount) {
            this.sharedCount = sharedCount;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getExt() {
            return ext;
        }

        public void setExt(String ext) {
            this.ext = ext;
        }

        public String getLargeThumbnailUrl() {
            return largeThumbnailUrl;
        }

        public File setLargeThumbnailUrl(String largeThumbnailUrl) {
            this.largeThumbnailUrl = largeThumbnailUrl;
            return this;
        }

        public String getMediumThumbnailUrl() {
            return mediumThumbnailUrl;
        }

        public File setMediumThumbnailUrl(String mediumThumbnailUrl) {
            this.mediumThumbnailUrl = mediumThumbnailUrl;
            return this;
        }

        public String getSmallThumbnailUrl() {
            return smallThumbnailUrl;
        }

        public File setSmallThumbnailUrl(String smallThumbnailUrl) {
            this.smallThumbnailUrl = smallThumbnailUrl;
            return this;
        }

        public String getThumbnailUrl() {
            return thumbnailUrl;
        }

        public File setThumbnailUrl(String thumbnailUrl) {
            this.thumbnailUrl = thumbnailUrl;
            return this;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Poll {
        private long creatorId;
        private long Id;
        private String subject;
        private long roomId;
        private int commentCount;

        public long getCreatorId() {
            return creatorId;
        }

        public void setCreatorId(long creatorId) {
            this.creatorId = creatorId;
        }

        public long getId() {
            return Id;
        }

        public void setId(long id) {
            Id = id;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public long getRoomId() {
            return roomId;
        }

        public void setRoomId(long roomId) {
            this.roomId = roomId;
        }

        public int getCommentCount() {
            return commentCount;
        }

        public void setCommentCount(int commentCount) {
            this.commentCount = commentCount;
        }
    }
}
