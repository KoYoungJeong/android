package com.tosslab.jandi.app.network.models;

/**
 * Created by Steve SeongUg Jung on 14. 12. 11..
 */
public class ReqCreateNewTeam {
    private final String name;
    private final String teamDomain;

    public ReqCreateNewTeam(String name, String teamDomain) {
        this.name = name;
        this.teamDomain = teamDomain;
    }

    public String getName() {
        return name;
    }

    public String getTeamDomain() {
        return teamDomain;
    }

}
