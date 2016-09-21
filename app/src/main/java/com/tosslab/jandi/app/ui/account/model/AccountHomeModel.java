package com.tosslab.jandi.app.ui.account.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.PollRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.invitation.InvitationApi;
import com.tosslab.jandi.app.network.client.settings.AccountProfileApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResPollList;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.AccountUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;


public class AccountHomeModel {

    private Lazy<InvitationApi> invitationApi;
    private Lazy<AccountApi> accountApi;
    private Lazy<AccountProfileApi> accountProfileApi;
    private Lazy<StartApi> startApi;
    private Lazy<PollApi> pollApi;

    @Inject
    public AccountHomeModel(Lazy<InvitationApi> invitationApi,
                            Lazy<AccountApi> accountApi,
                            Lazy<AccountProfileApi> accountProfileApi,
                            Lazy<StartApi> startApi,
                            Lazy<PollApi> pollApi) {
        this.invitationApi = invitationApi;
        this.accountApi = accountApi;
        this.accountProfileApi = accountProfileApi;
        this.startApi = startApi;
        this.pollApi = pollApi;
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

        List<ResAccountInfo.UserTeam> userTeams = AccountRepository.getRepository().getAccountTeams();
        teams.addAll(convertJoinedTeamList(userTeams));

        List<ResPendingTeamInfo> pendingTeamInfo = invitationApi.get().getPedingTeamInfo();
        for (int idx = pendingTeamInfo.size() - 1; idx >= 0; idx--) {
            if (!TextUtils.equals(pendingTeamInfo.get(idx).getStatus(), "pending")) {
                pendingTeamInfo.remove(idx);
            }
        }

        teams.addAll(convertPedingTeamList(pendingTeamInfo));
        teams.add(Team.createEmptyTeam());

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

    public ResAccountInfo updateAccountName(String newName) throws RetrofitException {
        return accountProfileApi.get().changeName(new ReqProfileName(newName));
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

    public ResAccountInfo.UserTeam getSelectedTeamInfo() {
        return AccountRepository.getRepository().getSelectedTeamInfo();
    }

    public ResAccountInfo.UserEmail getSelectedEmailInfo() {
        List<ResAccountInfo.UserEmail> userEmails = AccountRepository.getRepository()
                .getAccountEmails();
        for (ResAccountInfo.UserEmail userEmail : userEmails) {
            if (userEmail.isPrimary()) {
                return userEmail;
            }
        }
        return null;
    }

    public void updateTeamInfo(long teamId) throws RetrofitException {
        ResAccountInfo resAccountInfo = accountApi.get().getAccountInfo();
        AccountUtil.removeDuplicatedTeams(resAccountInfo);
        AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
    }

    public String getAccountName() {
        return AccountRepository.getRepository().getAccountInfo().getName();
    }

    public boolean checkAccount() {
        return AccountRepository.getRepository().getAccountInfo() != null;
    }

    public void refreshPollList(long teamId) {
        try {
            PollRepository.getInstance().clearAll();

            ResPollList resPollList = pollApi.get().getPollList(teamId, 50);
            List<Poll> onGoing = resPollList.getOnGoing();
            if (onGoing == null) {
                onGoing = new ArrayList<>();
            }
            List<Poll> finished = resPollList.getFinished();
            if (finished == null) {
                finished = new ArrayList<>();
            }
            Observable.merge(Observable.from(onGoing), Observable.from(finished))
                    .toList()
                    .subscribe(polls -> PollRepository.getInstance().upsertPollList(polls),
                            Throwable::printStackTrace);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
