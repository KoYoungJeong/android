package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.tosslab.jandi.app.local.orm.dao.InitializeInfoDaoImpl;
import com.tosslab.jandi.app.local.orm.persister.CollectionLongConverter;
import com.tosslab.jandi.app.network.jackson.deserialize.start.InitializeInfoConverter;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonDeserialize(converter = InitializeInfoConverter.class)
@DatabaseTable(tableName = "initial_info_base", daoClass = InitializeInfoDaoImpl.class)
public class InitialInfo {
    @JsonIgnore
    @DatabaseField(id = true)
    private long teamId;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Self self;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Team team;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Poll poll;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private Mention mention;
    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private TeamPlan teamPlan;

    @ForeignCollectionField(eager = true)
    private Collection<Folder> folders;
    @ForeignCollectionField(eager = true)
    private Collection<Topic> topics;
    @ForeignCollectionField(eager = true)
    private Collection<Chat> chats;
    @ForeignCollectionField(eager = true)
    private Collection<Human> members;
    @ForeignCollectionField(eager = true)
    private Collection<Bot> bots;

    @DatabaseField
    private long ts;

    @DatabaseField(persisterClass = CollectionLongConverter.class)
    private List<Long> starredMessageIds;

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Collection<Folder> getFolders() {
        return folders;
    }

    public void setFolders(Collection<Folder> folders) {
        this.folders = folders;
    }

    public Collection<Topic> getTopics() {
        return topics;
    }

    public void setTopics(Collection<Topic> topics) {
        this.topics = topics;
    }

    public Collection<Chat> getChats() {
        return chats;
    }

    public void setChats(Collection<Chat> chats) {
        this.chats = chats;
    }

    public Collection<Human> getMembers() {
        return members;
    }

    public void setMembers(Collection<Human> members) {
        this.members = members;
    }

    public Collection<Bot> getBots() {
        return bots;
    }

    public void setBots(Collection<Bot> bots) {
        this.bots = bots;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public Self getSelf() {
        return self;
    }

    public void setSelf(Self self) {
        this.self = self;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public List<Long> getStarredMessageIds() {
        return starredMessageIds;
    }

    public void setStarredMessageIds(List<Long> starredMessageIds) {
        this.starredMessageIds = starredMessageIds;
    }

    public Poll getPoll() {
        return poll;
    }

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public Mention getMention() {
        return mention;
    }

    public void setMention(Mention mention) {
        this.mention = mention;
    }

    public TeamPlan getTeamPlan() {
        return teamPlan;
    }

    public void setTeamPlan(TeamPlan teamPlan) {
        this.teamPlan = teamPlan;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @DatabaseTable(tableName = "initial_info_self")
    public static class Self {
        @DatabaseField(id = true)
        private long id;
        @DatabaseField
        private String name;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Self{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @DatabaseTable(tableName = "initial_poll_info")
    public static class Poll {
        @DatabaseField(id = true)
        private long id;

        @DatabaseField
        private int votableCount;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public int getVotableCount() {
            return votableCount;
        }

        public void setVotableCount(int votableCount) {
            this.votableCount = votableCount;
        }

        @Override
        public String toString() {
            return "Poll{" +
                    "votableCount=" + votableCount +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @DatabaseTable(tableName = "initial_mention_info")
    public static class Mention {
        @DatabaseField(id = true)
        private long id;

        @DatabaseField
        private int unreadCount;
        @DatabaseField
        private long lastMentionedMessageId;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public int getUnreadCount() {
            return unreadCount;
        }

        public void setUnreadCount(int unreadCount) {
            this.unreadCount = unreadCount;
        }

        public long getLastMentionedMessageId() {
            return lastMentionedMessageId;
        }

        public void setLastMentionedMessageId(long lastMentionedMessageId) {
            this.lastMentionedMessageId = lastMentionedMessageId;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @DatabaseTable(tableName = "initial_team_plan_info")
    public static class TeamPlan {
        @DatabaseField(id = true)
        private long id;
        @DatabaseField
        private long teamId;
        @DatabaseField
        private String pricing;
        @DatabaseField
        private boolean isExceedFile;
        @DatabaseField
        private boolean isExceedMessage;
        @DatabaseField
        private Date updatedAt;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getTeamId() {
            return teamId;
        }

        public void setTeamId(long teamId) {
            this.teamId = teamId;
        }

        public String getPricing() {
            return pricing;
        }

        public void setPricing(String pricing) {
            this.pricing = pricing;
        }

        public boolean isExceedFile() {
            return isExceedFile;
        }

        public void setExceedFile(boolean exceedFile) {
            isExceedFile = exceedFile;
        }

        public boolean isExceedMessage() {
            return isExceedMessage;
        }

        public void setExceedMessage(boolean exceedMessage) {
            isExceedMessage = exceedMessage;
        }

        public Date getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(Date updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

}
