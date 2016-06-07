package com.tosslab.jandi.app.push.model;

import android.app.ActivityManager;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.ChatRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.PushTokenRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.client.account.AccountApi;
import com.tosslab.jandi.app.network.client.main.ConfigApi;
import com.tosslab.jandi.app.network.client.rooms.RoomsApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.PushToken;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResRoomInfo;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.push.to.PushRoomType;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.ApplicationUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;


/**
 * Created by Steve SeongUg Jung on 15. 1. 26..
 */
@EBean
public class JandiInterfaceModel {

    @SystemService
    ActivityManager activityManager;

    @Inject
    Lazy<ConfigApi> configApi;
    @Inject
    Lazy<AccountApi> accountApi;
    @Inject
    Lazy<RoomsApi> roomsApi;

    @Inject
    Lazy<StartApi> startApi;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent
                .create()
                .inject(this);
    }

    public int getInstalledAppVersion() {
        return ApplicationUtil.getAppVersionCode();
    }

    public ResConfig getConfigInfo() throws RetrofitException {
        return configApi.get().getConfig();
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

            if (InitialInfoRepository.getInstance().getInitialInfo(teamId) != null) {
                return true;
            } else {
                return getEntityInfo();
            }

        } else {
            return true;
        }
    }

    public boolean getEntityInfo() {
        try {
            InitialInfo initializeInfo = startApi.get().getInitializeInfo(AccountRepository.getRepository().getSelectedTeamId());
            InitialInfoRepository.getInstance().upsertInitialInfo(initializeInfo);
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
    public Pair<Boolean, Long> getEntityInfo(long teamId, long roomId, String roomType) {

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
                entityId = roomId;
            }
        }


        if (!isChatType(roomType)) {
            // Room Type 은 RoomId = EntityId
            if (hasEntity(roomId)) {
                entityId = roomId;
            } else {
                entityRefreshed = getEntityInfo();
                entityId = roomId;
            }
        } else {
            long chatMemberId = getChatMemberId(teamId, roomId);

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

    private long getChatMemberId(long teamId, long roomId) {
        ResChat chat = ChatRepository.getRepository().getChatByRoom(roomId);

        if (chat != null && chat.getEntityId() > 0) {
            // 캐시된 정보로 확인
            return chat.getCompanionId();
        } else {
            // 서버로부터 요청
            try {
                ResRoomInfo roomInfo = roomsApi.get().getRoomInfo(teamId, roomId);

                if (roomInfo != null) {

                    long myId = TeamInfoLoader.getInstance().getMyId();

                    for (long member : roomInfo.getMembers()) {
                        if (myId != member) {
                            return member;
                        }
                    }

                }
            } catch (RetrofitException retrofitError) {
                retrofitError.printStackTrace();
                return -1;
            }
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

}
