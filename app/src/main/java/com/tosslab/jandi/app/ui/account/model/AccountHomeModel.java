package com.tosslab.jandi.app.ui.account.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.BadgeCountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.invitation.InvitationApi;
import com.tosslab.jandi.app.network.client.main.LeftSideApi;
import com.tosslab.jandi.app.network.client.settings.AccountProfileApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.mixpanel.MixpanelAccountAnalyticsClient;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;


/**
 * Created by Steve SeongUg Jung on 15. 3. 3..
 */
@EBean
public class AccountHomeModel {

    @Inject
    Lazy<InvitationApi> invitationApi;
    @Inject
    Lazy<LeftSideApi> leftSideApi;
    @Inject
    Lazy<AccountApi> accountApi;
    @Inject
    Lazy<AccountProfileApi> accountProfileApi;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create().inject(this);
    }

    public void refreshAccountInfo() {
        try {
            ResAccountInfo resAccountInfo = accountApi.get().getAccountInfo();
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

    public ResLeftSideMenu getEntityInfo(long teamId) throws RetrofitException {
        return leftSideApi.get().getInfosForSideMenu(teamId);
    }

    public EntityManager updateEntityInfo(Context context, ResLeftSideMenu entityInfo) {
        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(entityInfo);
        int totalUnreadCount = BadgeUtils.getTotalUnreadCount(entityInfo);
        BadgeCountRepository badgeCountRepository = BadgeCountRepository.getRepository();
        badgeCountRepository.upsertBadgeCount(entityInfo.team.id, totalUnreadCount);
        BadgeUtils.setBadge(context, badgeCountRepository.getTotalBadgeCount());

        EntityManager entityManager = EntityManager.getInstance();
        entityManager.refreshEntity();
        return entityManager;
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

    public String getAccountName() {
        return AccountRepository.getRepository().getAccountInfo().getName();
    }

    public boolean checkAccount() {
        return AccountRepository.getRepository().getAccountInfo() != null;
    }

    public void trackLaunchTeamSuccess(long teamId) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.LaunchTeam)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.TeamId, teamId)
                        .build());

    }

    public void trackLaunchTeamFail(int errorCode) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.LaunchTeam)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.ErrorCode, errorCode)
                        .build());

    }

    public void trackChangeAccountNameSuccess(Context context, String accountId) {
        MixpanelAccountAnalyticsClient
                .getInstance(context, accountId)
                .trackSetAccount();

        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.ChangeAccountName)
                        .accountId(accountId)
                        .property(PropertyKey.ResponseSuccess, true)
                        .build());
    }

    public void trackChangeAccountNameFail(int errorCode) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.ChangeAccountName)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.ErrorCode, errorCode)
                        .build());


    }

}
