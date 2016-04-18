package com.tosslab.jandi.app.ui.team.info.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.client.validation.ValidationApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.validation.ResValidation;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;

@EBean
public class TeamDomainInfoModel {

    @Inject
    Lazy<AccountApi> accountApi;
    @Inject
    Lazy<ValidationApi> validationApi;
    @Inject
    Lazy<TeamApi> teamApi;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent
                .create()
                .inject(this);
    }

    public ResTeamDetailInfo createNewTeam(String name, String teamDomain) throws RetrofitException {

        ReqCreateNewTeam reqCreateNewTeam = new ReqCreateNewTeam(name, teamDomain);
        return teamApi.get().createNewTeam(reqCreateNewTeam);

    }

    public ResValidation validDomain(String domain) {
        try {
            return validationApi.get().validDomain(domain);
        } catch (RetrofitException retrofitError) {
            ResValidation resValidation = new ResValidation();
            resValidation.setIsValidate(false);
            return resValidation;
        }
    }

    public List<ResAccountInfo.UserEmail> initUserEmailInfo() {

        List<ResAccountInfo.UserEmail> userEmails = AccountRepository.getRepository().getAccountEmails();
        List<ResAccountInfo.UserEmail> filteredUserEmails = new ArrayList<>();

        Observable.from(userEmails)
                .filter(userEmail -> TextUtils.equals(userEmail.getStatus(), "confirmed"))
                .collect(() -> filteredUserEmails, List::add)
                .subscribe();

        return filteredUserEmails;
    }


    public void updateTeamInfo(long teamId) throws RetrofitException {

        ResAccountInfo resAccountInfo = accountApi.get().getAccountInfo();
        AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
    }

    public void trackCreateTeamSuccess(long teamId) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.CreateTeam)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.TeamId, teamId)
                .build());

    }

    public void trackCreateTeamFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.CreateTeam)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());

    }

}
