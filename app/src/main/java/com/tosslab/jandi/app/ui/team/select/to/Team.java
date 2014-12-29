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
    private final int unread;
    private Status status;
    private boolean isSelected;
    private String userEmail;
    private String token;

    private Team(int teamId, int memberId, String name, String teamDomain, Status status, int unread) {
        this.teamId = teamId;
        this.memberId = memberId;
        this.name = name;
        this.teamDomain = teamDomain;
        this.status = status;
        this.isSelected = false;
        this.unread = unread;
    }

    public static Team createTeam(ResAccountInfo.UserTeam userTeam) {
        return new Team(userTeam.getTeamId(), userTeam.getMemberId(), userTeam.getName(), userTeam.getTeamDomain(), Status.JOINED, userTeam.getUnread());
    }

    public static Team createTeam(ResPendingTeamInfo resPendingTeamInfo) {
        Team team = new Team(resPendingTeamInfo.getTeamId(), resPendingTeamInfo.getMemberId(), resPendingTeamInfo.getTeamName(), resPendingTeamInfo.getTeamDomain(), Status.PENDING, -1);
        team.setUserEmail(resPendingTeamInfo.getToEmail());
        team.setToken(resPendingTeamInfo.getToken());
        return team;
    }

    public static Team createEmptyTeam() {
        return new Team(0, 0, "", "", Status.CREATE, -1);
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "Team{" +
                "teamId=" + teamId +
                ", memberId=" + memberId +
                ", name='" + name + '\'' +
                ", teamDomain='" + teamDomain + '\'' +
                ", status=" + status +
                ", isSelected=" + isSelected +
                ", userEmail='" + userEmail + '\'' +
                '}';
    }

    public int getUnread() {
        return unread;
    }

    public enum Status {
        JOINED, PENDING, CREATE
    }
}
