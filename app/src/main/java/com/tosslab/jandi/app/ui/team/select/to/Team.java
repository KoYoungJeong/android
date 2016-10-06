package com.tosslab.jandi.app.ui.team.select.to;

import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;

public class Team {

    private final long teamId;
    private final long memberId;
    private final String name;
    private final String teamDomain;
    private int unread;
    private Status status;
    private boolean isSelected;
    private String userEmail;
    private String token;

    private String invitationId;

    private Team(long teamId, long memberId, String email, String name, String teamDomain, Status status, int unread, String invitationId) {
        this.teamId = teamId;
        this.memberId = memberId;
        this.name = name;
        this.teamDomain = teamDomain;
        this.status = status;
        this.isSelected = false;
        this.unread = unread;
        this.invitationId = invitationId;
        this.userEmail = email;
    }

    public static Team createTeam(ResAccountInfo.UserTeam userTeam) {
        return new Team(userTeam.getTeamId(), userTeam.getMemberId(), userTeam.getEmail(), userTeam.getName(), userTeam.getTeamDomain(), Status.JOINED, userTeam.getUnread(), "");
    }

    public static Team createTeam(ResPendingTeamInfo resPendingTeamInfo) {
        Team team = new Team(resPendingTeamInfo.getTeamId(), resPendingTeamInfo.getMemberId(), resPendingTeamInfo.getToEmail(), resPendingTeamInfo.getTeamName(), resPendingTeamInfo.getTeamDomain(), Status.PENDING, -1, resPendingTeamInfo.getId());
        team.setUserEmail(resPendingTeamInfo.getToEmail());
        team.setToken(resPendingTeamInfo.getToken());
        return team;
    }

    public static Team createEmptyTeam() {
        return new Team(0, 0, "", "", "", Status.CREATE, -1, "");
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public long getTeamId() {
        return teamId;
    }

    public long getMemberId() {
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

    public String getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(String invitationId) {
        this.invitationId = invitationId;
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
                ", invitationId='" + invitationId + '\'' +
                '}';
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public enum Status {
        JOINED, PENDING, CREATE
    }
}
