package com.tosslab.jandi.app.ui.search.main_temp.object;

/**
 * Created by tee on 16. 7. 25..
 */
public class SearchTopicRoomData extends SearchData {

    private long id;
    private boolean isPublic;
    private String title;
    private long memberCnt;
    private String description;
    private boolean isJoined;
    private boolean isStarred;

    private SearchTopicRoomData() {
    }

    public long getId() {
        return id;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public String getTitle() {
        return title;
    }

    public long getMemberCnt() {
        return memberCnt;
    }

    public String getDescription() {
        return description;
    }

    public boolean isJoined() {
        return isJoined;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public static class Builder {

        private SearchTopicRoomData searchTopicRoomData;

        public Builder() {
            searchTopicRoomData = new SearchTopicRoomData();
        }

        public Builder setTopicId(long id) {
            searchTopicRoomData.id = id;
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

        public SearchTopicRoomData build() {
            return searchTopicRoomData;
        }


    }

}
