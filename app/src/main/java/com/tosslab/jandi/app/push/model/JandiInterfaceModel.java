package com.tosslab.jandi.app.push.model;

import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.PollRepository;
import com.tosslab.jandi.app.local.orm.repositories.PushTokenRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.PushToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResPollList;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
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

    Lazy<AccountApi> accountApi;
    Lazy<StartApi> startApi;
    @Inject
    Lazy<PollApi> pollApi;

    @Inject
    public JandiInterfaceModel(Lazy<AccountApi> accountApi,
                               Lazy<StartApi> startApi) {
        this.accountApi = accountApi;
        this.startApi = startApi;
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

        if ((selectedTeamInfo == null || selectedTeamInfo.getTeamId() != teamId)) {

            AccountRepository.getRepository().updateSelectedTeamInfo(teamId);
            MessageRepository.getRepository().deleteAllLink();

            return getEntityInfo();

        } else {
            return true;
        }
    }

    public boolean getEntityInfo() {
        try {
            long selectedTeamId = AccountRepository.getRepository().getSelectedTeamId();
            InitialInfo initializeInfo = startApi.get().getInitializeInfo(selectedTeamId);
            InitialInfoRepository.getInstance().upsertInitialInfo(initializeInfo);
            TeamInfoLoader.getInstance().refresh();
            refreshPollList(selectedTeamId);
            JandiPreference.setSocketConnectedLastTime(initializeInfo.getTs());
            return true;
        } catch (RetrofitException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * LeftSideMenu 를 갱신함
     *
     * @return (갱신 여부, 요청한 entityId)
     */
    public Pair<Boolean, Long> getEntityInfo(long roomId, String roomType) {

        boolean entityRefreshed = false;
        long entityId = -1L;


        if (!isKnowRoomType(roomType)) {
            entityRefreshed = getEntityInfo();

            if (hasEntity(roomId)) {
                if (!TeamInfoLoader.getInstance().isUser(roomId)) {
                    roomType = PushRoomType.CHANNEL.getName();
                } else {
                    roomType = PushRoomType.CHAT.getName();
                }
            } else {
                entityId = -1;
            }
        }


        if (!isChatType(roomType)) {
            // Room Type 은 RoomId = EntityId
            if (hasEntity(roomId)) {
                entityId = roomId;
            } else {
                entityRefreshed = getEntityInfo();
                if (hasEntity(roomId)) {
                    entityId = roomId;
                } else {
                    entityId = -1;
                }
            }
        } else {
            long chatMemberId = getChatMemberId(roomId);

            if (!hasEntity(chatMemberId)) {
                entityRefreshed = getEntityInfo();
            }
            entityId = chatMemberId;
        }

        return new Pair<>(entityRefreshed, entityId);
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
}
