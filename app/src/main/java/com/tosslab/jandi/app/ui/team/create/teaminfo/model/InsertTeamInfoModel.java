package com.tosslab.jandi.app.ui.team.create.teaminfo.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.client.validation.ValidationApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqCreateNewTeam;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.validation.ResValidation;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;

/**
 * Created by tee on 16. 6. 24..
 */

public class InsertTeamInfoModel {

    private final Lazy<StartApi> startApi;
    private Lazy<AccountApi> accountApi;
    private Lazy<ValidationApi> validationApi;
    private Lazy<TeamApi> teamApi;

    @Inject
    public InsertTeamInfoModel(Lazy<TeamApi> teamApi,
                               Lazy<ValidationApi> validationApi,
                               Lazy<AccountApi> accountApi,
                               Lazy<StartApi> startApi) {
        this.teamApi = teamApi;
        this.validationApi = validationApi;
        this.accountApi = accountApi;
        this.startApi = startApi;
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


    public boolean isValidDomainCharacters(String domainName) {
        return domainName.matches("[a-zA-Z1-9\\-]+");
    }

    public boolean isValidDomainLength(String domainName) {
        return domainName.length() >= 3;
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
        AccountUtil.removeDuplicatedTeams(resAccountInfo);
        AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
        TeamInfoLoader.getInstance().refresh();
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

    public void updateEntityInfo(final long teamId) throws RetrofitException {
        AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
        InitialInfo initializeInfo = startApi.get().getInitializeInfo(teamId);
        InitialInfoRepository.getInstance().upsertInitialInfo(initializeInfo);
        JandiPreference.setSocketConnectedLastTime(initializeInfo.getTs());
        MessageRepository.getRepository().deleteAllLink();
    }
}
