package com.tosslab.jandi.app.network.models.poll;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.tosslab.jandi.app.local.orm.dao.PollDaoImpl;
import com.tosslab.jandi.app.local.orm.persister.CollectionIntegerConverter;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

@DatabaseTable(tableName = "poll", daoClass = PollDaoImpl.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Poll {
    @DatabaseField(id = true)
    private long id;
    @DatabaseField
    private Date dueDate;
    @DatabaseField
    private long creatorId;
    @DatabaseField
    private long topicId;
    @DatabaseField
    private long teamId;
    @DatabaseField
    private String subject;
    @DatabaseField
    private String description;
    @DatabaseField
    private Date finishedAt;
    @DatabaseField
    private Date updatedAt;
    @DatabaseField
    private Date createdAt;

    @ForeignCollectionField(foreignFieldName = "poll")
    private Collection<Item> electedItems; // 1등인 아이템들
    @DatabaseField
    private int votedCount;
    @DatabaseField
    private String status;
    @DatabaseField
    private boolean multipleChoice;
    @DatabaseField
    private boolean anonymous;
    @DatabaseField
    private int commentCount;

    @ForeignCollectionField(foreignFieldName = "poll")
    private Collection<Item> items;
    @DatabaseField
    private String voteStatus;

    @DatabaseField(persisterClass = CollectionIntegerConverter.class)
    private Collection<Integer> votedItemSeqs;

    private boolean isMine;

    private long messageId;
    private boolean isStarred;

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void setIsStarred(boolean isStarred) {
        this.isStarred = isStarred;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public long getTopicId() {
        return topicId;
    }

    public void setTopicId(long topicId) {
        this.topicId = topicId;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Date finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getVotedCount() {
        return votedCount;
    }

    public void setVotedCount(int votedCount) {
        this.votedCount = votedCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isMultipleChoice() {
        return multipleChoice;
    }

    public void setMultipleChoice(boolean multipleChoice) {
        this.multipleChoice = multipleChoice;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getVoteStatus() {
        return voteStatus;
    }

    public void setVoteStatus(String voteStatus) {
        this.voteStatus = voteStatus;
    }

    public Collection<Item> getElectedItems() {
        return electedItems;
    }

    public void setElectedItems(Collection<Item> electedItems) {
        this.electedItems = electedItems;
    }

    public Collection<Item> getItems() {
        return items;
    }

    public void setItems(Collection<Item> items) {
        this.items = items;
    }

    public Collection<Integer> getVotedItemSeqs() {
        return votedItemSeqs;
    }

    public void setVotedItemSeqs(Collection<Integer> votedItemSeqs) {
        this.votedItemSeqs = votedItemSeqs;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setIsMine(boolean mine) {
        isMine = mine;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (items != null && items.isEmpty()) {
            sb.append("[");
            Iterator<Item> iterator = items.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(iterator.next().toString());
                i++;
            }
            sb.append("]");
        }

        return "Poll{" +
                "id=" + id +
                ", dueDate=" + dueDate +
                ", creatorId=" + creatorId +
                ", topicId=" + topicId +
                ", teamId=" + teamId +
                ", subject='" + subject + '\'' +
                ", finishedAt=" + finishedAt +
                ", updatedAt=" + updatedAt +
                ", createdAt=" + createdAt +
                ", electedItems=" + electedItems +
                ", votedCount=" + votedCount +
                ", status='" + status + '\'' +
                ", isMine='" + isMine + '\'' +
                ", multipleChoice=" + multipleChoice +
                ", anonymous=" + anonymous +
                ", commentCount=" + commentCount +
                ", items=" + sb.toString() +
                ", voteStatus='" + voteStatus + '\'' +
                ", votedItemSeqs=" + votedItemSeqs +
                '}';
    }

    @DatabaseTable(tableName = "poll_item")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Item {
        @DatabaseField(generatedId = true)
        private long _id;

        @JsonIgnore
        @DatabaseField(foreign = true)
        private Poll poll;

        @DatabaseField
        private int seq;
        @DatabaseField
        private String name;
        @DatabaseField
        private int votedCount;

        @JsonIgnore
        @DatabaseField
        private boolean elected;

        public int getSeq() {
            return seq;
        }

        public void setSeq(int seq) {
            this.seq = seq;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getVotedCount() {
            return votedCount;
        }

        public void setVotedCount(int votedCount) {
            this.votedCount = votedCount;
        }

        public Poll getPoll() {
            return poll;
        }

        public void setPoll(Poll poll) {
            this.poll = poll;
        }

        public boolean isElected() {
            return elected;
        }

        public void setElected(boolean elected) {
            this.elected = elected;
        }

        public long get_id() {
            return _id;
        }

        public void set_id(long _id) {
            this._id = _id;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "seq=" + seq +
                    ", name='" + name + '\'' +
                    ", votedCount=" + votedCount +
                    '}';
        }
    }

}
