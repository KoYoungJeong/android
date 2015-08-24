package com.tosslab.jandi.app.ui.team.info.model;

import android.content.Context;
import android.text.TextUtils;

import com.google.android.gms.analytics.HitBuilders;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ReqInvitationAcceptOrIgnore;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SupposeBackground;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 14. 12. 18..
 */
@EBean
public class TeamDomainInfoModel {

    @RootContext
    Context context;

    private Callback callback;

    public ResTeamDetailInfo createNewTeam(String name, String teamDomain) throws RetrofitError {

        ReqCreateNewTeam reqCreateNewTeam = new ReqCreateNewTeam(name, teamDomain);
        return RequestApiManager.getInstance().createNewTeamByTeamApi(reqCreateNewTeam);

    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public List<ResAccountInfo.UserEmail> initUserEmailInfo() {

        List<ResAccountInfo.UserEmail> userEmails = AccountRepository.getRepository().getAccountEmails();

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
    public ResTeamDetailInfo acceptOrDclineInvite(String invitationId, String type) throws RetrofitError {

        ResAccountInfo accountInfo = AccountRepository.getRepository().getAccountInfo();

        if (accountInfo == null) {
            return null;
        }

        ReqInvitationAcceptOrIgnore reqInvitationAcceptOrIgnore = new ReqInvitationAcceptOrIgnore(type);
        ResTeamDetailInfo resTeamDetailInfos = RequestApiManager.getInstance().
                acceptOrDeclineInvitationByInvitationApi(invitationId, reqInvitationAcceptOrIgnore);
        return resTeamDetailInfos;

    }

    public ResTeamDetailInfo.InviteTeam getTeamInfo(int teamId) throws RetrofitError {
        ResTeamDetailInfo.InviteTeam resTeamDetailInfo = RequestApiManager.getInstance().getTeamInfoByTeamApi(teamId);
        return resTeamDetailInfo;
    }

    public void updateTeamInfo(int teamId) {

        ResAccountInfo resAccountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();
        AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
    }

    public String getUserName() {
        return AccountRepository.getRepository().getAccountInfo().getName();
    }
    
    public void trackCreateTeamSuccess(int teamId) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.CreateTeam)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.TeamId, teamId)
                        .build());

        try {
            ((JandiApplication) JandiApplication.getContext()).getTracker(JandiApplication.TrackerName.APP_TRACKER)
                    .send(new HitBuilders.EventBuilder()
                            .setCategory(Event.CreateTeam.name())
                            .setAction("ResponseSuccess")
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void trackCreateTeamFail(int errorCode) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.CreateTeam)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.ErrorCode, errorCode)
                        .build());

        try {
            ((JandiApplication) JandiApplication.getContext()).getTracker(JandiApplication.TrackerName.APP_TRACKER)
                    .send(new HitBuilders.EventBuilder()
                            .setCategory(Event.CreateTeam.name())
                            .setAction("ResponseFail")
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    public interface Callback {
        void onTeamCreateSuccess(String name, int memberId, int teamId);

        void onTeamCreateFail(int statusCode);
    }


}
