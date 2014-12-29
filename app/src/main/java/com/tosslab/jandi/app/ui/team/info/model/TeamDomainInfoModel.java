package com.tosslab.jandi.app.ui.team.info.model;

import android.content.Context;

import com.tosslab.jandi.app.local.database.JandiDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.ui.team.select.model.AcceptInviteRequest;
import com.tosslab.jandi.app.ui.team.select.model.AccountInfoRequest;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
@EBean
public class TeamDomainInfoModel {

    private final static Logger logger = Logger.getLogger(TeamDomainInfoModel.class);

    @RestService
    JandiRestClient jandiRestClient;

    @RootContext
    Context context;

    private Callback callback;

    @Background
    public void createNewTeam(String name, String teamDomain, String myName, String myEmail) {

        ReqCreateNewTeam reqCreateNewTeam = new ReqCreateNewTeam(name, teamDomain, myName, myEmail);
        TeamCreateRequest teamCreateRequest = TeamCreateRequest.create(context, reqCreateNewTeam);
        RequestManager<ResTeamDetailInfo> requestManager = RequestManager.newInstance(context, teamCreateRequest);

        try {
            ResTeamDetailInfo resTeamDetailInfo = requestManager.request();
            if (callback != null) {
                callback.onTeamCreateSuccess(name);
            }
        } catch (JandiNetworkException e) {
            if (callback != null) {
                callback.onTeamCreateFail(e.httpStatusCode);
            }
        }

    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public List<ResAccountInfo.UserEmail> initUserEmailInfo() {
        return JandiDatabaseManager.getInstance(context).getUserEmails();
    }

    @SupposeBackground
    public ResTeamDetailInfo acceptInvite(String token, String userEmail, String name) {

        ResAccountInfo accountInfo = JandiDatabaseManager.getInstance(context).getAccountInfo();

        if (accountInfo == null) {
            return null;
        }

        AcceptInviteRequest request = AcceptInviteRequest.create(context, token, userEmail, name);
        RequestManager<ResTeamDetailInfo> requestManager = RequestManager.newInstance(context, request);
        try {
            ResTeamDetailInfo resTeamDetailInfos = requestManager.request();
            return resTeamDetailInfos;
        } catch (JandiNetworkException e) {
            e.printStackTrace();
            return null;
        }


    }

    public void updateTeamInfo(int teamId) {
        AccountInfoRequest accountInfoRequest = AccountInfoRequest.create(context);
        RequestManager<ResAccountInfo> resAccountInfoRequestManager = RequestManager.newInstance(context, accountInfoRequest);
        ResAccountInfo resAccountInfo = null;
        try {
            resAccountInfo = resAccountInfoRequestManager.request();
            JandiDatabaseManager.getInstance(context).upsertAccountTeams(resAccountInfo.getMemberships());
            JandiDatabaseManager.getInstance(context).updateSelectedTeam(teamId);
        } catch (JandiNetworkException e) {


        }
    }

    public interface Callback {
        void onTeamCreateSuccess(String name);

        void onTeamCreateFail(int statusCode);
    }


}
