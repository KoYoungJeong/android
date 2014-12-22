package com.tosslab.jandi.app.ui.team.select.model;

import android.content.Context;

import com.tosslab.jandi.app.local.database.JandiDatabaseManager;
import com.tosslab.jandi.app.network.ResultObject;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.invitation.InvitationApiClient;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResPendingTeamInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.rest.RestService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 14. 12. 17..
 */
@EBean
public class TeamSelectionModel {

    @RootContext
    Context context;

    @RestService
    JandiRestClient jandiRestClient;

    @RestService
    InvitationApiClient invitationApiClient;

    @SupposeBackground
    public ResultObject<ArrayList<Team>> getMyTeamList() {

        ArrayList<Team> teams = new ArrayList<Team>();

        try {

            List<ResAccountInfo.UserTeam> userTeams = JandiDatabaseManager.getInstance(context).getUserTeams();

            teams.addAll(convertJoinedTeamList(userTeams));

            PendingTeamListRequest pendingTeamListRequest = PendingTeamListRequest.create(context);
            RequestManager<List<ResPendingTeamInfo>> pendingTeaListManager = RequestManager.newInstance(context, pendingTeamListRequest);

            List<ResPendingTeamInfo> pedingTeamInfo = pendingTeaListManager.request();

            teams.addAll(convertPedingTeamList(pedingTeamInfo));


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


    @SupposeBackground
    public List<ResPendingTeamInfo> ignoreInvite(Team team) {

        IgnoreInviteRequest ignoreInviteRequest = IgnoreInviteRequest.create(context, team);
        RequestManager<List<ResPendingTeamInfo>> requestManager = RequestManager.newInstance(context, ignoreInviteRequest);
        try {
            List<ResPendingTeamInfo> resPendingTeamInfos = requestManager.request();
            return resPendingTeamInfos;
        } catch (JandiNetworkException e) {
            e.printStackTrace();
            return null;
        }

    }

    @SupposeBackground
    public List<ResTeamDetailInfo> acceptInvite(Team team) {

        ResAccountInfo accountInfo = JandiDatabaseManager.getInstance(context).getAccountInfo();

        if (accountInfo == null) {
            return null;
        }

        AcceptInviteRequest request = AcceptInviteRequest.create(context, team, accountInfo.getName());
        RequestManager<List<ResTeamDetailInfo>> requestManager = RequestManager.newInstance(context, request);
        try {
            List<ResTeamDetailInfo> resTeamDetailInfos = requestManager.request();
            return resTeamDetailInfos;
        } catch (JandiNetworkException e) {
            e.printStackTrace();
            return null;
        }


    }

    public void updateToDBJoinedTeamInfo() {
        // Team
        AccountInfoRequest accountInfoRequest = AccountInfoRequest.create(context);
        RequestManager<ResAccountInfo> resAccountInfoRequestManager = RequestManager.newInstance(context, accountInfoRequest);
        ResAccountInfo resAccountInfo = null;
        try {
            resAccountInfo = resAccountInfoRequestManager.request();
            JandiDatabaseManager.getInstance(context).upsertAccountTeams(resAccountInfo.getMemberships());
        } catch (JandiNetworkException e) {


        }
    }

    public void updateSelectedTeam(Team lastSelectedItem) {
        JandiDatabaseManager.getInstance(context).updateSelectedTeam(lastSelectedItem.getTeamId());
    }
}
