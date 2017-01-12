package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.jackson.deserialize.start.InitializeInfoConverter;
import com.vimeo.stag.GsonAdapterKey;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonDeserialize(converter = InitializeInfoConverter.class)
public class InitialInfo {
    @GsonAdapterKey
    Self self;
    @GsonAdapterKey
    Team team;
    @GsonAdapterKey
    Poll poll;
    @GsonAdapterKey
    Mention mention;
    @GsonAdapterKey
    TeamPlan teamPlan;

    @GsonAdapterKey
    List<Folder> folders;
    @GsonAdapterKey
    List<Topic> topics;
    @GsonAdapterKey
    List<Chat> chats;
    @GsonAdapterKey
    List<Human> members;
    @GsonAdapterKey
    List<Bot> bots;

    @GsonAdapterKey
    long ts;

    @GsonAdapterKey
    List<Long> starredMessageIds;

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }

    public List<Chat> getChats() {
        return chats;
    }

    public void setChats(List<Chat> chats) {
        this.chats = chats;
    }

    public List<Human> getMembers() {
        return members;
    }

    public void setMembers(List<Human> members) {
        this.members = members;
    }

    public List<Bot> getBots() {
        return bots;
    }

    public void setBots(List<Bot> bots) {
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

}
