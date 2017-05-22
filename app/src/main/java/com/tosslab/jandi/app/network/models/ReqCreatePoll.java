package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.databind.util.ISO8601Utils;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tonyjs on 16. 6. 14..
 */
public class ReqCreatePoll {
    private long topicId;
    private String subject;
    private String description;
    private boolean anonymous;
    private boolean multipleChoice;
    private String dueDate;
    private List<String> items;
    private String comment;
    private List<MentionObject> mentions;

    private ReqCreatePoll(long topicId, String subject, String description,
                          boolean anonymous, boolean multipleChoice,
                          Date dueDate, List<String> items) {
        this.topicId = topicId;
        this.subject = subject;
        this.description = description;
        this.anonymous = anonymous;
        this.multipleChoice = multipleChoice;
        this.dueDate = ISO8601Utils.format(dueDate);
        this.items = items;
    }

    public static ReqCreatePoll create(long topicId, String subject, String description,
                                       boolean anonymous, boolean multipleChoice,
                                       Date dueDate, List<String> items) {
        return new ReqCreatePoll(topicId, subject, description, anonymous, multipleChoice, dueDate, items);
    }

    public ReqCreatePoll comment(String comment) {
        this.comment = comment;
        return this;
    }

    public ReqCreatePoll mentions(List<MentionObject> mentions) {
        this.mentions = mentions;
        return this;
    }

    public long getTopicId() {
        return topicId;
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public boolean isMultipleChoice() {
        return multipleChoice;
    }

    public String getDueDate() {
        return dueDate;
    }

    public List<String> getItems() {
        return items;
    }

    public String getComment() {
        return comment;
    }

    public List<MentionObject> getMentions() {
        return mentions;
    }

    @Override
    public String toString() {
        return "ReqCreatePoll{" +
                "topicId=" + topicId +
                ", subject='" + subject + '\'' +
                ", anonymous=" + anonymous +
                ", multipleChoice=" + multipleChoice +
                ", dueDate='" + dueDate + '\'' +
                ", items=" + items +
                ", comment='" + comment + '\'' +
                ", mentions=" + mentions +
                '}';
    }

    public static class Builder {
        private final long topicId;
        private String subject;
        private boolean anonymous;
        private boolean multipleChoice;
        private Calendar dueDate;
        private int hour = -1;
        private Map<Integer, String> itemsMap = new HashMap<>();
        private List<String> items;
        private String comment;
        private List<MentionObject> mentions;
        private String description;

        public Builder(long topicId) {
            this.topicId = topicId;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder anonymous(boolean anonymous) {
            this.anonymous = anonymous;
            return this;
        }

        public Builder multipleChoice(boolean multipleChoice) {
            this.multipleChoice = multipleChoice;
            return this;
        }

        public Builder dueDate(Calendar dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public Builder hour(int hour) {
            this.hour = hour;
            return this;
        }

        public Builder putItemToMap(int position, String item) {
            this.itemsMap.put(position, item);
            return this;
        }

        public Builder removeItemFromMap(int position) {
            try {
                this.itemsMap.remove(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this;
        }

        public Builder items(List<String> items) {
            this.items = items;
            return this;
        }

        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder mentions(List<MentionObject> mentions) {
            this.mentions = mentions;
            return this;
        }

        public long getTopicId() {
            return topicId;
        }

        public String getSubject() {
            return subject;
        }

        public String getDescription() {
            return description;
        }

        public boolean isAnonymous() {
            return anonymous;
        }

        public boolean isMultipleChoice() {
            return multipleChoice;
        }

        public Calendar getDueDate() {
            return dueDate;
        }

        public int getHour() {
            return hour;
        }

        public Map<Integer, String> getItemsMap() {
            return itemsMap;
        }

        public List<String> getItems() {
            return items;
        }

        public String getComment() {
            return comment;
        }

        public List<MentionObject> getMentions() {
            return mentions;
        }

        public ReqCreatePoll build() {
            ReqCreatePoll reqCreatePoll = new ReqCreatePoll(topicId,
                    subject, description, anonymous, multipleChoice,
                    dueDate.getTime(), items);
            reqCreatePoll.comment(comment);
            reqCreatePoll.mentions(mentions);
            return reqCreatePoll;
        }
    }

}