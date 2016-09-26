package com.tosslab.jandi.app.ui.maintab.model;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.client.main.ConfigApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.DirectMessageRoom;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.JandiPreference;

import dagger.Lazy;
import rx.Observable;

/**
 * Created by tonyjs on 2016. 8. 23..
 */
public class MainTabModel {

    private final Lazy<ConfigApi> configApi;
    private final Lazy<StartApi> startApi;

    public MainTabModel(Lazy<ConfigApi> configApi, Lazy<StartApi> startApi) {
        this.configApi = configApi;
        this.startApi = startApi;
    }

    public int getTopicBadgeCount() {
        return Observable.from(TeamInfoLoader.getInstance().getTopicList())
                .filter(TopicRoom::isJoined)
                .map(TopicRoom::getUnreadCount)
                .scan((lhs, rhs) -> lhs + rhs)
                .toBlocking()
                .lastOrDefault(0);
    }

    public int getChatBadgeCount() {
        return Observable.from(TeamInfoLoader.getInstance().getDirectMessageRooms())
                .filter(DirectMessageRoom::isJoined)
                .map(DirectMessageRoom::getUnreadCount)
                .scan((lhs, rhs) -> lhs + rhs)
                .toBlocking()
                .lastOrDefault(0);
    }

    public int getMyPageBadgeCount() {
        return TeamInfoLoader.getInstance().getPollBadge();
    }

    public Observable<ResConfig> getConfigInfoObservable() {
        return Observable.defer(() -> {
            try {
                ResConfig resConfig = configApi.get().getConfig();
                return Observable.just(resConfig);
            } catch (RetrofitException e) {
                return Observable.error(e);
            }
        });
    }

    public int getCurrentAppVersionCode() {
        return ApplicationUtil.getAppVersionCode();
    }

    public Observable<User> getMeObservable() {
        return Observable.defer(() -> {
            long myId = TeamInfoLoader.getInstance().getMyId();
            User me = TeamInfoLoader.getInstance().getUser(myId);
            if (me != null) {
                return Observable.just(me);
            } else {
                return Observable.empty();
            }
        });
    }

    public Observable<Object> getRefreshEntityInfoObservable() {
        return Observable.create(subscriber -> {
            try {
                InitialInfo initializeInfo = startApi.get().getInitializeInfo(TeamInfoLoader.getInstance().getTeamId());
                InitialInfoRepository.getInstance().upsertInitialInfo(initializeInfo);
                JandiPreference.setSocketConnectedLastTime(initializeInfo.getTs());
            } catch (Exception error) {
                subscriber.onError(error);
                return;
            }
            TeamInfoLoader.getInstance().refresh();
            subscriber.onNext(new Object());
            subscriber.onCompleted();
        });
    }

    public boolean needInvitePopup() {
        int memberCount = TeamInfoLoader.getInstance().getUserList().size();
        return JandiPreference.isInvitePopup(JandiApplication.getContext()) && memberCount <= 1;
    }
}
