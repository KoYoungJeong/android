package com.tosslab.jandi.app.ui.maintab.tabs.team.vo;

import com.tosslab.jandi.app.team.member.User;

import java.util.List;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public class Team {
    private long id;
    private String name;
    private String domain;
    private User owner;
    private List<User> members;

    private Team(long id, String name, String domain, User owner, List<User> members) {
        this.id = id;
        this.name = name;
        this.domain = domain;
        this.owner = owner;
        this.members = members;
    }

    public static Team create(long id, String name, String domain, User owner, List<User> members) {
        return new Team(id, name, domain, owner, members);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public User getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", owner=" + owner +
                '}';
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public String getDomain() {
        return domain;
    }

    public List<User> getMembers() {
        return members;
    }
}
