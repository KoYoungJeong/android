package com.tosslab.jandi.app.ui.search.main_temp.object;

/**
 * Created by tee on 16. 7. 25..
 */
public class SearchTopicRoomData extends SearchData {

    private long topicId;
    private boolean isPublic;
    private String title;
    private long memberCnt;
    private String description;
    private boolean isJoined;
    private boolean isStarred;
    private String keyword;
    private boolean hasHalfLine;

    private SearchTopicRoomData() {
    }

    public long getTopicId() {
        return topicId;
    }

    public void setTopicId(long topicId) {
        this.topicId = topicId;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getMemberCnt() {
        return memberCnt;
    }

    public void setMemberCnt(long memberCnt) {
        this.memberCnt = memberCnt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isJoined() {
        return isJoined;
    }

    public void setJoined(boolean joined) {
        isJoined = joined;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void setStarred(boolean starred) {
        isStarred = starred;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public boolean hasHalfLine() {
        return hasHalfLine;
    }

    public void setHasHalfLine(boolean hasHalfLine) {
        this.hasHalfLine = hasHalfLine;
    }

    public static class Builder {

        private SearchTopicRoomData searchTopicRoomData;

        public Builder() {
            searchTopicRoomData = new SearchTopicRoomData();
        }

        public Builder setTopicId(long id) {
            searchTopicRoomData.topicId = id;
            return this;
        }

        public Builder setTitle(String title) {
            searchTopicRoomData.title = title;
            return this;
        }

        public Builder setIsPublic(boolean isPublic) {
            searchTopicRoomData.isPublic = isPublic;
            return this;
        }

        public Builder setMemberCnt(long memberCnt) {
            searchTopicRoomData.memberCnt = memberCnt;
            return this;
        }

        public Builder setDescription(String description) {
            searchTopicRoomData.description = description;
            return this;
        }

        public Builder setIsJoined(boolean isJoined) {
            searchTopicRoomData.isJoined = isJoined;
            return this;
        }

        public Builder setIsStarred(boolean isStarred) {
            searchTopicRoomData.isStarred = isStarred;
            return this;
        }

        public Builder setKeyword(String keyword) {
            searchTopicRoomData.keyword = keyword;
            return this;
        }

        public Builder setHasHalfLine(boolean hasHalfLine) {
            searchTopicRoomData.hasHalfLine = hasHalfLine;
            return this;
        }

        public SearchTopicRoomData build() {
            return searchTopicRoomData;
        }

    }

}
