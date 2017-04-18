package com.tosslab.jandi.app.push.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.PollRepository;
import com.tosslab.jandi.app.local.orm.repositories.PushTokenRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.ChatRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.RankRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.events.EventsApi;
import com.tosslab.jandi.app.network.client.marker.MarkerApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.PushToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResOnlineStatus;
import com.tosslab.jandi.app.network.models.ResPollList;
import com.tosslab.jandi.app.network.models.marker.Marker;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.RawInitialInfo;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.network.models.team.rank.Ranks;
import com.tosslab.jandi.app.push.to.PushRoomType;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.Room;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.JandiPreference;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;


public class JandiInterfaceModel {

    private Lazy<MarkerApi> markerApi;
    private Lazy<AccountApi> accountApi;
    private Lazy<StartApi> startApi;
    private Lazy<EventsApi> eventApi;
    private Lazy<PollApi> pollApi;
    private Lazy<TeamApi> teamApi;

    @Inject
    public JandiInterfaceModel(Lazy<AccountApi> accountApi,
                               Lazy<StartApi> startApi, Lazy<EventsApi> eventApi,
                               Lazy<PollApi> pollApi, Lazy<TeamApi> teamApi,
                               Lazy<MarkerApi> markerApi) {
        this.accountApi = accountApi;
        this.startApi = startApi;
        this.eventApi = eventApi;
        this.pollApi = pollApi;
        this.teamApi = teamApi;
        this.markerApi = markerApi;
    }

    public void refreshAccountInfo() throws RetrofitException {
        ResAccountInfo resAccountInfo = accountApi.get().getAccountInfo();

        AccountUtil.removeDuplicatedTeams(resAccountInfo);
        AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);

    }

    public boolean hasTeamInfo(long teamId) {
        ResAccountInfo.UserTeam teamInfo = AccountRepository.getRepository().getTeamInfo(teamId);

        if (teamInfo == null) {

            try {
                refreshAccountInfo();
                teamInfo = AccountRepository.getRepository().getTeamInfo(teamId);

                if (teamInfo == null) {
                    return false;
                }

            } catch (RetrofitException e) {
                e.printStackTrace();
                return false;
            }

        }
        return true;
    }

    public boolean setupSelectedTeam(long teamId) {

        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();

        if (selectedTeamInfo == null || selectedTeamInfo.getTeamId() != teamId) {

            AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
            MessageRepository.getRepository().deleteAllLink();

            return refreshTeamInfo();

        } else {
            refreshRankInfoIfNeed(selectedTeamInfo.getTeamId());
            return true;
        }
    }

    public boolean refreshTeamInfo() {
        try {
            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            String initializeInfo = startApi.get().getRawInitializeInfo(selectedTeamId);
            InitialInfoRepository.getInstance().upsertRawInitialInfo(new RawInitialInfo(selectedTeamId, initializeInfo));
            if (!refreshRankInfoIfNeed(selectedTeamId)) {
                TeamInfoLoader.getInstance().refresh();
            }
            updateOnlineStatus(selectedTeamId);
            refreshMyMarker();
            refreshPollList(selectedTeamId);
            return true;
        } catch (RetrofitException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void refreshMyMarker() {
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

    private boolean refreshRankInfoIfNeed(long selectedTeamId) {
        if (!RankRepository.getInstance().hasRanks(selectedTeamId)) {
            try {
                Ranks ranks = teamApi.get().getRanks(selectedTeamId);
                RankRepository.getInstance().addRanks(ranks.getRanks());
                TeamInfoLoader.getInstance().refresh();
                return true;
            } catch (RetrofitException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void updateOnlineStatus(long teamId) throws RetrofitException {
        ResOnlineStatus resOnlineStatus = teamApi.get().getOnlineStatus(teamId);
        TeamInfoLoader.getInstance().setOnlineStatus(resOnlineStatus.getRecords());
    }

    /**
     * LeftSideMenu 를 갱신함
     *
     * @return (갱신 여부, 요청한 entityId)
     */
    public long getEntityInfo(long roomId, String roomType) {

        long entityId = -1L;

        try {
            TeamInfoLoader.getInstance().getMyId();
        } catch (Exception e) {
            refreshTeamInfo();
        }


        if (!isKnowRoomType(roomType)) {
            refreshTeamInfo();

            if (hasEntity(roomId)) {
                if (!TeamInfoLoader.getInstance().isUser(roomId)) {
                    roomType = PushRoomType.CHANNEL.getName();
                } else {
                    roomType = PushRoomType.CHAT.getName();
                }
            } else {
                entityId = -1;
            }
        } else if (!isChatType(roomType)) {
            // Room Type 은 RoomId = EntityId
            if (hasEntity(roomId)) {
                entityId = roomId;
            } else {
                refreshTeamInfo();
                if (hasEntity(roomId)) {
                    entityId = roomId;
                } else {
                    entityId = -1;
                }
            }
        } else {
            long chatMemberId = getChatMemberId(roomId);

            if (!hasEntity(chatMemberId)) {
                refreshTeamInfo();
            }
            entityId = chatMemberId;
        }

        return entityId;
    }

    private boolean isKnowRoomType(String roomTypeRaw) {
        return Observable.from(PushRoomType.values())
                .filter(roomType -> TextUtils.equals(roomTypeRaw, roomType.getName()))
                .map(roomType1 -> true)
                .firstOrDefault(false)
                .toBlocking()
                .first();
    }

    private boolean hasEntity(long roomId) {
        return TeamInfoLoader.getInstance().isTopic(roomId)
                || TeamInfoLoader.getInstance().isUser(roomId);
    }

    private long getChatMemberId(long roomId) {
        Room room = TeamInfoLoader.getInstance().getRoom(roomId);

        if (room != null && room.getId() > 0) {
            // 캐시된 정보로 확인
            return Observable.from(room.getMembers())
                    .takeFirst(memberId -> memberId != TeamInfoLoader.getInstance().getMyId())
                    .toBlocking()
                    .firstOrDefault(-1L);
        }
        return -1;
    }

    private boolean isChatType(String roomType) {
        return TextUtils.equals(roomType, PushRoomType.CHAT.getName());
    }

    public boolean hasNotRegisteredAtNewPushService() {
        List<PushToken> pushTokenList = PushTokenRepository.getInstance().getPushTokenList();
        return pushTokenList.isEmpty();
    }

    public int getEventHistoryCount() {
        long ts = JandiPreference.getSocketConnectedLastTime();
        long myId = TeamInfoLoader.getInstance().getMyId();
        try {
            return eventApi.get().getEventHistory(ts, myId, 1).getTotal();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void refreshPollList(long teamId) {
        try {
            PollRepository.getInstance().clearAll();

            ResPollList resPollList = pollApi.get().getPollList(teamId, 50);
            List<Poll> onGoing = resPollList.getOnGoing();
            if (onGoing == null) {
                onGoing = new ArrayList<>();
            }
            List<Poll> finished = resPollList.getFinished();
            if (finished == null) {
                finished = new ArrayList<>();
            }
            Observable.merge(Observable.from(onGoing), Observable.from(finished))
                    .toList()
                    .subscribe(polls -> PollRepository.getInstance().upsertPollList(polls),
                            Throwable::printStackTrace);
        } catch (RetrofitException retrofitError) {
            retrofitError.printStackTrace();
        }
    }

    public boolean hasRank() {
        return RankRepository.getInstance().hasRanks(TeamInfoLoader.getInstance().getTeamId());
    }
}
