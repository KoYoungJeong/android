package com.tosslab.jandi.app.ui.team.select.to;

import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;

/**
 * Created by Steve SeongUg Jung on 14. 12. 17..
 */
public class Team {

    private final int teamId;
    private final int memberId;
    private final String name;
    private final String teamDomain;
    private Status status;
    private boolean isSelected;

    private Team(int teamId, int memberId, String name, String teamDomain, Status status) {
        this.teamId = teamId;
        this.memberId = memberId;
        this.name = name;
        this.teamDomain = teamDomain;
        this.status = status;
        this.isSelected = false;
    }

    public static Team createTeam(ResAccountInfo.UserTeam userTeam) {
        return new Team(userTeam.getTeamId(), userTeam.getMemberId(), userTeam.getName(), userTeam.getTeamDomain(), Status.JOINED);
    }

    public static Team createTeam(ResPendingTeamInfo resPendingTeamInfo) {
        return new Team(resPendingTeamInfo.getTeamId(), resPendingTeamInfo.getMemberId(), resPendingTeamInfo.getTeamName(), resPendingTeamInfo.getTeamDomain(), Status.PENDING);
    }

    public static Team createEmptyTeam() {
        return new Team(0, 0, "", "", Status.CREATE);
    }

    public int getTeamId() {
        return teamId;
    }

    public int getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }

    public String getTeamDomain() {
        return teamDomain;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public enum Status {
        JOINED, PENDING, CREATE
    }


}
