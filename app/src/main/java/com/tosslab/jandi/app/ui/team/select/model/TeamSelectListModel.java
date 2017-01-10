package com.tosslab.jandi.app.ui.team.select.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.RankRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.invitation.InvitationApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.team.rank.Ranks;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.AccountUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;

/**
 * Created by tee on 2016. 9. 27..
 */

public class TeamSelectListModel {

    private Lazy<InvitationApi> invitationApi;
    private Lazy<AccountApi> accountApi;
    private Lazy<StartApi> startApi;
    private Lazy<TeamApi> teamApi;

    @Inject
    public TeamSelectListModel(Lazy<InvitationApi> invitationApi,
                               Lazy<AccountApi> accountApi,
                               Lazy<StartApi> startApi,
                               Lazy<TeamApi> teamApi) {
        this.invitationApi = invitationApi;
        this.accountApi = accountApi;
        this.startApi = startApi;
        this.teamApi = teamApi;
    }

    public void refreshAccountInfo() {
        try {
            ResAccountInfo resAccountInfo = accountApi.get().getAccountInfo();
            AccountUtil.removeDuplicatedTeams(resAccountInfo);
            AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
        } catch (RetrofitException retrofitError) {
            retrofitError.printStackTrace();
        }
    }

    public ResTeamDetailInfo acceptOrDeclineInvite(String invitationId, String type) throws RetrofitException {

        ResAccountInfo accountInfo = AccountRepository.getRepository().getAccountInfo();

        if (accountInfo == null) {
            return null;
        }

        ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore = new ReqInvitationAcceptOrIgnore(type);
        return invitationApi.get().acceptOrDeclineInvitation(invitationId, reqInvitationAcceptOrIgnore);
    }

    public List<Team> getTeamInfos() throws RetrofitException {
        List<Team> teams = new ArrayList<Team>();

        List<ResPendingTeamInfo> pendingTeamInfo = invitationApi.get().getPedingTeamInfo();

        for (int idx = pendingTeamInfo.size() - 1; idx >= 0; idx--) {
            if (!TextUtils.equals(pendingTeamInfo.get(idx).getStatus(), "pending")) {
                pendingTeamInfo.remove(idx);
            }
        }

        teams.addAll(convertPedingTeamList(pendingTeamInfo));

        List<ResAccountInfo.UserTeam> userTeams = AccountRepository.getRepository().getAccountTeams();
        teams.addAll(convertJoinedTeamList(userTeams));

        teams.add(Team.createEmptyTeam());

        return teams;
    }

    private List<Team> convertJoinedTeamList(List<ResAccountInfo.UserTeam> memberships) {
        List<Team> teams = new ArrayList<Team>();

        if (memberships == null) {
            return teams;
        }

        for (ResAccountInfo.UserTeam membership : memberships) {
            teams.add(Team.createTeam(membership));
        }

        return teams;
    }

    private List<Team> convertPedingTeamList(List<ResPendingTeamInfo> pedingTeamInfos) {
        List<Team> teams = new ArrayList<Team>();

        if (pedingTeamInfos == null) {
            return teams;
        }

        for (ResPendingTeamInfo pedingTeamInfo : pedingTeamInfos) {
            teams.add(Team.createTeam(pedingTeamInfo));
        }

        return teams;
    }

    public void updateSelectTeam(long teamId) {
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
    }

    public InitialInfo getEntityInfo(long teamId) throws RetrofitException {
        return startApi.get().getInitializeInfo(teamId);
    }

    public void updateEntityInfo(InitialInfo entityInfo) {
        InitialInfoRepository.getInstance().upsertInitialInfo(entityInfo);
    }

    public void updateTeamInfo(long teamId) throws RetrofitException {
        ResAccountInfo resAccountInfo = accountApi.get().getAccountInfo();
        AccountUtil.removeDuplicatedTeams(resAccountInfo);
        AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
    }

    public String getMyEmail() {
        List<ResAccountInfo.UserEmail> emails = AccountRepository.getRepository().getAccountEmails();
        if (emails == null || emails.isEmpty()) {
            return "";
        }
        int length = emails.size();
        String primaryEmail = emails.get(0).getEmail();
        for (int i = 0; i < length; i++) {
            if (emails.get(i).isPrimary()) {
                primaryEmail = emails.get(i).getEmail();
            }
        }
        return primaryEmail;
    }

    public void refreshRankIfNeed(long teamId) {
        if (!RankRepository.getInstance().hasRanks(teamId)) {
            try {
                Ranks ranks = teamApi.get().getRanks(teamId);
                RankRepository.getInstance().addRanks(ranks.getRanks());
            } catch (RetrofitException e) {
                e.printStackTrace();
            }
        }

    }
}