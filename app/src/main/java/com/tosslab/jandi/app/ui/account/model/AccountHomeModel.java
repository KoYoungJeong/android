package com.tosslab.jandi.app.ui.account.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.ResultObject;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.ui.profile.account.model.AccountNameChangeRequest;
import com.tosslab.jandi.app.ui.team.select.model.PendingTeamListRequest;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 3. 3..
 */
@EBean
public class AccountHomeModel {


    public ResultObject<ArrayList<Team>> getTeamInfos(Context context) {

        ArrayList<Team> teams = new ArrayList<Team>();

        try {

            List<ResAccountInfo.UserTeam> userTeams = JandiAccountDatabaseManager.getInstance(context).getUserTeams();

            teams.addAll(convertJoinedTeamList(userTeams));

            PendingTeamListRequest pendingTeamListRequest = PendingTeamListRequest.create(context);
            RequestManager<List<ResPendingTeamInfo>> pendingTeaListManager = RequestManager.newInstance(context, pendingTeamListRequest);

            List<ResPendingTeamInfo> pedingTeamInfo = pendingTeaListManager.request();

            for (int idx = pedingTeamInfo.size() - 1; idx >= 0; idx--) {
                if (!TextUtils.equals(pedingTeamInfo.get(idx).getStatus(), "pending")) {
                    pedingTeamInfo.remove(idx);
                }
            }

            teams.addAll(convertPedingTeamList(pedingTeamInfo));
            teams.add(Team.createEmptyTeam());

            return ResultObject.createSuccessResult(teams);
        } catch (JandiNetworkException e) {
            e.printStackTrace();
            return ResultObject.createFailResult(e.httpStatusCode, e.httpBody, new ArrayList<Team>());
        } catch (Exception e) {
            return ResultObject.createFailResult(400, "", new ArrayList<Team>());
        }

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

    public ResAccountInfo updateAccountName(Context context, String newName) throws JandiNetworkException {
        return RequestManager.newInstance(context, AccountNameChangeRequest.create(context, newName)).request();
    }

    public void updateSelectTeam(Context context, int teamId) {
        JandiAccountDatabaseManager.getInstance(context).updateSelectedTeam(teamId);
    }

    public ResLeftSideMenu getEntityInfo(final Context context, int teamId) throws JandiNetworkException {
        return RequestManager.newInstance(context, () -> {
            JandiRestClient mJandiRestClient = new JandiRestClient_(context);
            mJandiRestClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
            return mJandiRestClient.getInfosForSideMenu(teamId);
        }).request();

    }

    public EntityManager updateEntityInfo(Context context, ResLeftSideMenu entityInfo) {

        JandiEntityDatabaseManager.getInstance(context).upsertLeftSideMenu(entityInfo);
        int totalUnreadCount = BadgeUtils.getTotalUnreadCount(entityInfo);
        JandiPreference.setBadgeCount(context, totalUnreadCount);
        BadgeUtils.setBadge(context, totalUnreadCount);

        EntityManager entityManager = EntityManager.getInstance(context);
        entityManager.refreshEntity(context);
        return entityManager;
    }

    public ResAccountInfo.UserTeam getSelectedTeamInfo(Context context) {
        return JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo();
    }

    public ResAccountInfo.UserEmail getSelectedEmailInfo(Context context) {
        List<ResAccountInfo.UserEmail> userEmails = JandiAccountDatabaseManager.getInstance(context).getUserEmails();
        for (ResAccountInfo.UserEmail userEmail : userEmails) {
            if (userEmail.isPrimary()) {
                return userEmail;
            }
        }
        return null;
    }

    public String getAccountName(Context context) {
        return JandiAccountDatabaseManager.getInstance(context).getAccountInfo().getName();
    }

}
