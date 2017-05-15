package com.tosslab.jandi.app.ui.intro.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.ChatRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.RankRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.events.EventsApi;
import com.tosslab.jandi.app.network.client.main.ConfigApi;
import com.tosslab.jandi.app.network.client.marker.MarkerApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResOnlineStatus;
import com.tosslab.jandi.app.network.models.marker.Marker;
import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.RawInitialInfo;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.network.models.team.rank.Ranks;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrSignIn;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;

public class IntroActivityModel {

    private final Lazy<TeamApi> teamApi;
    Lazy<AccountApi> accountApi;
    Lazy<StartApi> startApi;
    Lazy<ConfigApi> configApi;
    Lazy<EventsApi> eventApi;
    Lazy<MarkerApi> markerApi;

    @Inject
    public IntroActivityModel(Lazy<AccountApi> accountApi,
                              Lazy<StartApi> startApi,
                              Lazy<ConfigApi> configApi,
                              Lazy<EventsApi> eventApi,
                              Lazy<TeamApi> teamApi,
                              Lazy<MarkerApi> markerApi) {
        this.accountApi = accountApi;
        this.startApi = startApi;
        this.configApi = configApi;
        this.eventApi = eventApi;
        this.teamApi = teamApi;
        this.markerApi = markerApi;
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
            String initialInfo = startApi.get().getRawInitializeInfo(selectedTeamId);
            InitialInfoRepository.getInstance().upsertRawInitialInfo(new RawInitialInfo(selectedTeamId, initialInfo));
            if (!refreshRankIfNeeds()) {
                TeamInfoLoader.getInstance().refresh();
            }
            refreshMyMarker();
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

    public void trackSignInFailAndFlush(int errorCode) {
        SprinklrSignIn.sendFailLog(errorCode);
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

    public long getSelectedTeamId() {
        return AccountRepository.getRepository().getSelectedTeamId();
    }

    public int clearLinkRepository() {
        return MessageRepository.getRepository().deleteAllLink();
    }

    public boolean refreshRankIfNeeds() {
        long selectedTeam = TeamInfoLoader.getInstance().getTeamId();

        if (!RankRepository.getInstance().hasRanks(selectedTeam)) {
            try {
                Ranks ranks = teamApi.get().getRanks(selectedTeam);
                RankRepository.getInstance().addRanks(ranks.getRanks());
                TeamInfoLoader.getInstance().refresh();
                return true;
            } catch (RetrofitException e) {
                e.printStackTrace();
                return false;
            }

        } else {
            return true;
        }
    }

    public void refreshMyMarker() {
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        long myId = TeamInfoLoader.getInstance().getMyId();

        try {
            List<Marker> markers = markerApi.get().getMarkersFromMemberId(teamId, myId);
            for (Marker marker : markers) {
                long roomId = marker.getRoomId();
                long lastLinkId = marker.getReadLinkId();
                if (TeamInfoLoader.getInstance().isTopic(roomId)) {
                    TopicRepository.getInstance(teamId).updateReadLinkId(roomId, lastLinkId);
                    TopicRepository.getInstance(teamId).updateUnreadCount(roomId, 0);
                } else if (TeamInfoLoader.getInstance().isChat(roomId)) {
                    ChatRepository.getInstance(teamId).updateReadLinkId(roomId, lastLinkId);
                    ChatRepository.getInstance(teamId).updateUnreadCount(roomId, 0);
                }
            }

            Observable.concat(
                    Observable.from(TopicRepository.getInstance(teamId).getJoinedTopics())
                            .map(Topic::getUnreadCount),
                    Observable.from(ChatRepository.getInstance(teamId).getOpenedChats())
                            .map(Chat::getUnreadCount))
                    .filter(count -> count > 0)
                    .defaultIfEmpty(0)
                    .reduce((integer, integer2) -> integer + integer2)
                    .subscribe(count -> {
                        AccountRepository.getRepository().updateUnread(teamId, count);
                    }, Throwable::printStackTrace);

        } catch (RetrofitException e) {
            e.printStackTrace();
        }
    }

    public void updateOnlineStatus(long teamId) throws RetrofitException {
        ResOnlineStatus resOnlineStatus = teamApi.get().getOnlineStatus(teamId);
        TeamInfoLoader.getInstance().removeAllOnlineStatus();
        TeamInfoLoader.getInstance().setOnlineStatus(resOnlineStatus.getRecords());
    }

    public boolean hasRank() {
        return RankRepository.getInstance().hasRanks(TeamInfoLoader.getInstance().getTeamId());
    }

}