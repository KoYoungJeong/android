package com.tosslab.jandi.app.ui.maintab.team.vo;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import java.util.List;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public class Team {
    private long id;
    private String name;
    private String domain;
    private ResLeftSideMenu.User owner;
    private List<FormattedEntity> members;

    private Team(long id, String name, String domain, ResLeftSideMenu.User owner) {
        this.id = id;
        this.name = name;
        this.domain = domain;
        this.owner = owner;
    }

    public static Team create(long id, String name, String domain, ResLeftSideMenu.User owner) {
        return new Team(id, name, domain, owner);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ResLeftSideMenu.User getOwner() {
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

    public void setMembers(List<FormattedEntity> members) {
        this.members = members;
    }

    public String getDomain() {
        return domain;
    }

    public List<FormattedEntity> getMembers() {
        return members;
    }
}
