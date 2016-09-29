package com.tosslab.jandi.app.ui.intro.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.events.EventsApi;
import com.tosslab.jandi.app.network.client.main.ConfigApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.lib.sprinkler.io.domain.track.FutureTrack;

import javax.inject.Inject;

import dagger.Lazy;

public class IntroActivityModel {

    Lazy<AccountApi> accountApi;
    Lazy<StartApi> startApi;
    Lazy<ConfigApi> configApi;
    Lazy<EventsApi> eventApi;

    @Inject
    public IntroActivityModel(Lazy<AccountApi> accountApi,
                              Lazy<StartApi> startApi,
                              Lazy<ConfigApi> configApi,
                              Lazy<EventsApi> eventApi) {
        this.accountApi = accountApi;
        this.startApi = startApi;
        this.configApi = configApi;
        this.eventApi = eventApi;
    }

    public boolean isNetworkConnected() {
        return NetworkCheckUtil.isConnected();
    }

    public int getInstalledAppVersion() {
        return ApplicationUtil.getAppVersionCode();
    }

    public boolean isNeedLogin() {
        return TextUtils.isEmpty(TokenUtil.getRefreshToken());
    }

    public boolean refreshEntityInfo() {
        ResAccountInfo.UserTeam selectedTeamInfo =
                AccountRepository.getRepository().getSelectedTeamInfo();
        if (selectedTeamInfo == null) {
            return false;
        }
        try {
            long selectedTeamId = selectedTeamInfo.getTeamId();
            InitialInfo initialInfo = startApi.get().getInitializeInfo(selectedTeamId);
            InitialInfoRepository.getInstance().upsertInitialInfo(initialInfo);
            TeamInfoLoader.getInstance().refresh(initialInfo);
            JandiPreference.setSocketConnectedLastTime(initialInfo.getTs());
            return true;
        } catch (RetrofitException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ResConfig getConfigInfo() throws RetrofitException {
        return configApi.get().getConfig();
    }

    public boolean hasMigration() {
        ResAccountInfo accountInfo = AccountRepository.getRepository().getAccountInfo();
        return accountInfo != null && !TextUtils.isEmpty(accountInfo.getId());
    }

    public void trackAutoSignInSuccessAndFlush(boolean hasTeamSelected) {
        FutureTrack.Builder builder = new FutureTrack.Builder()
                .event(SprinklerEvents.SignIn)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.AutoSignIn, true);

        if (hasTeamSelected) {
            builder.memberId(AccountUtil.getMemberId(JandiApplication.getContext()));
        }

        AnalyticsUtil.trackSprinkler(builder.build());
        AnalyticsUtil.flushSprinkler();
    }

    public void trackSignInFailAndFlush(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(SprinklerEvents.SignIn)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());
        AnalyticsUtil.flushSprinkler();
    }

    public boolean hasLeftSideMenu() {
        long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
        boolean hasInitInfo = InitialInfoRepository.getInstance().hasInitialInfo(selectedTeamId);

        if (hasInitInfo) {
            try {
                return TeamInfoLoader.getInstance().getTeamId() > 0
                        && TeamInfoLoader.getInstance().getMyId() > 0;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean hasSelectedTeam() {
        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        return selectedTeamInfo != null;
    }

    public int clearLinkRepository() {
        return MessageRepository.getRepository().deleteAllLink();
    }

    public long getSelectedTeam() {
        return TeamInfoLoader.getInstance().getTeamId();
    }
}
