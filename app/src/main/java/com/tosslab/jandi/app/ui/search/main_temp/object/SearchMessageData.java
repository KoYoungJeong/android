package com.tosslab.jandi.app.ui.search.main_temp.object;

import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.search.ResSearch;

import java.util.Date;
import java.util.List;

/**
 * Created by tee on 16. 7. 25..
 */
public class SearchMessageData extends SearchData {

    private long roomId;
    private long linkId;
    private long messageId;
    private long writerId;
    private String text;
    private Date createdAt;
    private String feedbackType;
    private ResSearch.Poll poll;
    private ResSearch.File file;
    private String[] tokens;
    private List<MentionObject> mentions;

    private SearchMessageData() {
    }

    public long getRoomId() {
        return roomId;
    }

    public long getLinkId() {
        return linkId;
    }

    public long getMessageId() {
        return messageId;
    }

    public long getWriterId() {
        return writerId;
    }

    public String getText() {
        return text;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    public List<MentionObject> getMentions() {
        return mentions;
    }

    public String[] getTokens() {
        return tokens;
    }

    public ResSearch.Poll getPoll() {
        return poll;
    }

    public ResSearch.File getFile() {
        return file;
    }

    public static class Builder {

        private SearchMessageData searchMessageData;

        public Builder() {
            searchMessageData = new SearchMessageData();
            searchMessageData.setType(SearchData.ITEM_TYPE_MESSAGE_ITEM);
        }

        public Builder setRoomId(long roomId) {
            searchMessageData.roomId = roomId;
            return this;
        }

        public Builder setLinkId(long linkId) {
            searchMessageData.linkId = linkId;
            return this;
        }

        public Builder setMessageId(long messageId) {
            searchMessageData.messageId = messageId;
            return this;
        }

        public Builder setWriterId(long writerId) {
            searchMessageData.writerId = writerId;
            return this;
        }

        public Builder setText(String text) {
            searchMessageData.text = text;
            return this;
        }

        public Builder setCreatedAt(Date createdAt) {
            searchMessageData.createdAt = createdAt;
            return this;
        }

        public Builder setFeedbackType(String feedbackType) {
            searchMessageData.feedbackType = feedbackType;
            return this;
        }

        public Builder setMentions(List<MentionObject> mentions) {
            searchMessageData.mentions = mentions;
            return this;
        }

        public Builder setPoll(ResSearch.Poll poll) {
            searchMessageData.poll = poll;
            return this;
        }

        public Builder setFile(ResSearch.File file) {
            searchMessageData.file = file;
            return this;
        }

        public Builder setTokens(String[] tokens) {
            searchMessageData.tokens = tokens;
            return this;
        }


        public SearchMessageData build() {
            return searchMessageData;
        }

    }

}
