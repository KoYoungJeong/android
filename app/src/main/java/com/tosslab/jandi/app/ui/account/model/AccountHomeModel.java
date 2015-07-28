package com.tosslab.jandi.app.ui.account.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 3. 3..
 */
@EBean
public class AccountHomeModel {

    public List<Team> getTeamInfos(Context context) throws RetrofitError {

        ArrayList<Team> teams = new ArrayList<Team>();

        List<ResAccountInfo.UserTeam> userTeams = AccountRepository.getRepository().getAccountTeams();

        teams.addAll(convertJoinedTeamList(userTeams));

        List<ResPendingTeamInfo> pendingTeamInfo = RequestApiManager.getInstance().getPendingTeamInfoByInvitationApi();
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

    public ResAccountInfo updateAccountName(String newName) throws RetrofitError {
        return RequestApiManager.getInstance().changeNameByAccountProfileApi(new ReqProfileName(newName));
    }

    public void updateSelectTeam(int teamId) {
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
    }

    public ResLeftSideMenu getEntityInfo(int teamId) throws RetrofitError {
        return RequestApiManager.getInstance().getInfosForSideMenuByMainRest(teamId);
    }

    public EntityManager updateEntityInfo(Context context, ResLeftSideMenu entityInfo) {
        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(entityInfo);
        int totalUnreadCount = BadgeUtils.getTotalUnreadCount(entityInfo);
        JandiPreference.setBadgeCount(context, totalUnreadCount);
        BadgeUtils.setBadge(context, totalUnreadCount);

        EntityManager entityManager = EntityManager.getInstance(context);
        entityManager.refreshEntity(entityInfo);
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
}
