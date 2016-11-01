package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.jackson.deserialize.start.InitializeInfoConverter;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonDeserialize(converter = InitializeInfoConverter.class)
public class InitialInfo extends RealmObject {
    @JsonIgnore
    @PrimaryKey
    private long teamId;
    private Self self;
    private Team team;
    private Poll poll;
    private Mention mention;
    private TeamPlan teamPlan;

    private RealmList<Folder> folders;
    private RealmList<Topic> topics;
    private RealmList<Chat> chats;
    private RealmList<Human> members;
    private RealmList<Bot> bots;

    private long ts;

    @Ignore
    private List<Long> starredMessageIds;

    private RealmList<RealmLong> starredMessages;

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public RealmList<Folder> getFolders() {
        return folders;
    }

    public void setFolders(RealmList<Folder> folders) {
        this.folders = folders;
    }

    public RealmList<Topic> getTopics() {
        return topics;
    }

    public void setTopics(RealmList<Topic> topics) {
        this.topics = topics;
    }

    public RealmList<Chat> getChats() {
        return chats;
    }

    public void setChats(RealmList<Chat> chats) {
        this.chats = chats;
    }

    public RealmList<Human> getMembers() {
        return members;
    }

    public void setMembers(RealmList<Human> members) {
        this.members = members;
    }

    public RealmList<Bot> getBots() {
        return bots;
    }

    public void setBots(RealmList<Bot> bots) {
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

    public RealmList<RealmLong> getStarredMessages() {
        return starredMessages;
    }

    public void setStarredMessages(RealmList<RealmLong> starredMessages) {
        this.starredMessages = starredMessages;
    }

}
