package com.tosslab.jandi.app.network.models;

/**
 * Created by Steve SeongUg Jung on 14. 12. 11..
 */
public class ReqCreateNewTeam {
    private final String name;
    private final String teamDomain;
    private final String memberName;
    private final String memberEmail;

    public ReqCreateNewTeam(String name, String teamDomain, String memberName, String memberEmail) {
        this.name = name;
        this.teamDomain = teamDomain;
        this.memberName = memberName;
        this.memberEmail = memberEmail;
    }

    public String getName() {
        return name;
    }

    public String getTeamDomain() {
        return teamDomain;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getMemberEmail() {
        return memberEmail;
    }
}
