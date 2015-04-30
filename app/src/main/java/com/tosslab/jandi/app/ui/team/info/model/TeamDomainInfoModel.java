package com.tosslab.jandi.app.ui.team.info.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.ui.team.select.model.AcceptOrIgnoreInviteRequest;
import com.tosslab.jandi.app.ui.team.select.model.AccountInfoRequest;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.rest.RestService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
@EBean
public class TeamDomainInfoModel {

    @RestService
    JandiRestClient jandiRestClient;

    @RootContext
    Context context;

    private Callback callback;

    public ResTeamDetailInfo createNewTeam(String name, String teamDomain) throws JandiNetworkException {

        ReqCreateNewTeam reqCreateNewTeam = new ReqCreateNewTeam(name, teamDomain);
        TeamCreateRequest teamCreateRequest = TeamCreateRequest.create(context, reqCreateNewTeam);
        RequestManager<ResTeamDetailInfo> requestManager = RequestManager.newInstance(context, teamCreateRequest);

        return requestManager.request();

    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public List<ResAccountInfo.UserEmail> initUserEmailInfo() {
        List<ResAccountInfo.UserEmail> userEmails = JandiAccountDatabaseManager.getInstance(context).getUserEmails();

        Iterator<ResAccountInfo.UserEmail> confirmed = Observable.from(userEmails)
                .filter(userEmail -> TextUtils.equals(userEmail.getStatus(), "confirmed"))
                .toBlocking()
                .getIterator();

        List<ResAccountInfo.UserEmail> filteredUserEmails = new ArrayList<ResAccountInfo.UserEmail>();

        while (confirmed.hasNext()) {
            filteredUserEmails.add(confirmed.next());
        }

        return filteredUserEmails;
    }

    @SupposeBackground
    public ResTeamDetailInfo acceptOrDclineInvite(String invitationId, String type) throws JandiNetworkException {

        ResAccountInfo accountInfo = JandiAccountDatabaseManager.getInstance(context).getAccountInfo();

        if (accountInfo == null) {
            return null;
        }

        AcceptOrIgnoreInviteRequest request = AcceptOrIgnoreInviteRequest.create(context, invitationId, type);
        RequestManager<ResTeamDetailInfo> requestManager = RequestManager.newInstance(context, request);
        ResTeamDetailInfo resTeamDetailInfos = requestManager.request();
        return resTeamDetailInfos;

    }

    public ResTeamDetailInfo.InviteTeam getTeamInfo(int teamId) throws JandiNetworkException {
        TeamInfoRequest request = TeamInfoRequest.create(context, teamId);
        RequestManager<ResTeamDetailInfo.InviteTeam> requestManager = RequestManager.newInstance(context, request);
        ResTeamDetailInfo.InviteTeam resTeamDetailInfo = requestManager.request();
        return resTeamDetailInfo;
    }

    public void updateTeamInfo(int teamId) {
        AccountInfoRequest accountInfoRequest = AccountInfoRequest.create(context);
        RequestManager<ResAccountInfo> resAccountInfoRequestManager = RequestManager.newInstance(context, accountInfoRequest);
        ResAccountInfo resAccountInfo = null;
        try {
            resAccountInfo = resAccountInfoRequestManager.request();
            JandiAccountDatabaseManager databaseManager = JandiAccountDatabaseManager.getInstance(context);

            databaseManager.upsertAccountAllInfo(resAccountInfo);
            databaseManager.updateSelectedTeam(teamId);
        } catch (JandiNetworkException e) {


        }
    }

    public String getUserName() {
        return JandiAccountDatabaseManager.getInstance(context).getAccountInfo().getName();
    }

    public interface Callback {
        void onTeamCreateSuccess(String name, int memberId, int teamId);

        void onTeamCreateFail(int statusCode);
    }


}
