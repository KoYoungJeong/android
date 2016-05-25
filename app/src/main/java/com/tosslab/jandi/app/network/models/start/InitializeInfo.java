package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.tosslab.jandi.app.local.orm.dao.InitializeInfoDaoImpl;
import com.tosslab.jandi.app.network.jackson.deserialize.start.InitializeInfoConverter;

import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonDeserialize(converter = InitializeInfoConverter.class)
@DatabaseTable(tableName = "initial_info_base", daoClass = InitializeInfoDaoImpl.class)
public class InitializeInfo {
    @JsonIgnore
    @DatabaseField(id = true)
    private long teamId;
    @DatabaseField(foreign = true)
    private Self self;
    @DatabaseField(foreign = true)
    private Team team;
    @ForeignCollectionField
    private Collection<Folder> folders;
    @ForeignCollectionField
    private Collection<Topic> topics;
    @ForeignCollectionField
    private Collection<Chat> chats;
    @ForeignCollectionField
    private Collection<Human> humans;
    @ForeignCollectionField
    private Collection<Bot> bots;
    @DatabaseField
    private long ts;

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

    public Collection<Human> getHumans() {
        return humans;
    }

    public void setHumans(Collection<Human> humans) {
        this.humans = humans;
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
}
