package com.tosslab.jandi.app.push.model;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.BadgeCountRepository;
import com.tosslab.jandi.app.local.orm.repositories.ChatRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.network.models.ResConfig;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResRoomInfo;
import com.tosslab.jandi.app.push.to.PushTO;
import com.tosslab.jandi.app.utils.BadgeUtils;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;

import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 26..
 */
@EBean
public class JandiInterfaceModel {

    @RootContext
    Context context;

    @SystemService
    ActivityManager activityManager;

    public int getInstalledAppVersion(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            return 0;
        }
    }

    public ResConfig getConfigInfo() throws RetrofitError {
        return RequestApiManager.getInstance().getConfigByMainRest();
    }

    public void refreshAccountInfo() throws RetrofitError {
        ResAccountInfo resAccountInfo = RequestApiManager.getInstance().getAccountInfoByMainRest();

        AccountRepository.getRepository().upsertAccountAllInfo(resAccountInfo);

        EntityClientManager entityClientManager = EntityClientManager_.getInstance_(context);
        ResLeftSideMenu totalEntitiesInfo = entityClientManager.getTotalEntitiesInfo();
        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);

        int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
        BadgeCountRepository badgeCountRepository = BadgeCountRepository.getRepository();
        badgeCountRepository.upsertBadgeCount(totalEntitiesInfo.team.id, totalUnreadCount);
        BadgeUtils.setBadge(context, badgeCountRepository.getTotalBadgeCount());
    }

    public boolean hasBackStackActivity() {

        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);

        return tasks != null && tasks.size() > 0 && tasks.get(0).numActivities > 1;
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

            } catch (RetrofitError e) {
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

            return getEntityInfo();

        } else {

            if (hasBackStackActivity()) {
                return true;
            }

            try {
                EntityManager.getInstance();
                return true;
            } catch (Exception e) {
                return getEntityInfo();
            }

        }
    }

    private boolean getEntityInfo() {
        try {
            EntityClientManager entityClientManager = EntityClientManager_.getInstance_(context);
            ResLeftSideMenu totalEntitiesInfo = entityClientManager.getTotalEntitiesInfo();
            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(totalEntitiesInfo);
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(totalEntitiesInfo);
            BadgeCountRepository badgeCountRepository = BadgeCountRepository.getRepository();
            badgeCountRepository.upsertBadgeCount(totalEntitiesInfo.team.id, totalUnreadCount);
            BadgeUtils.setBadge(context, badgeCountRepository.getTotalBadgeCount());
            EntityManager.getInstance().refreshEntity();
            return true;
        } catch (RetrofitError e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long getEntityId(long teamId, long roomId, String roomType) {

        if (!isKnowRoomType(roomType)) {
            getEntityInfo();
            if (hasEntity(roomId)) {
                if (!EntityManager.getInstance().getEntityById(roomId).isUser()) {
                    roomType = PushTO.RoomType.CHANNEL.getName();
                } else {
                    roomType = PushTO.RoomType.CHAT.getName();
                }
            } else {
                return roomId;
            }
        }


        if (!isChatType(roomType)) {
            // Room Type 은 RoomId = EntityId
            if (hasEntity(roomId)) {
                return roomId;
            } else {
                getEntityInfo();
                return roomId;
            }
        } else {
            EntityManager entityManager = EntityManager.getInstance();
            long chatMemberId = getChatMemberId(teamId, roomId, entityManager);

            if (!hasEntity(chatMemberId)) {
                getEntityInfo();
            }
            return chatMemberId;
        }
    }

    private boolean isKnowRoomType(String roomTypeRaw) {
        return Observable.from(PushTO.RoomType.values())
                .filter(roomType -> TextUtils.equals(roomTypeRaw, roomType.getName()))
                .map(roomType1 -> true)
                .firstOrDefault(false)
                .toBlocking()
                .first();
    }

    private boolean hasEntity(long roomId) {
        return EntityManager.getInstance().getEntityById(roomId) != EntityManager.UNKNOWN_USER_ENTITY;
    }

    private long getChatMemberId(long teamId, long roomId, EntityManager entityManager) {
        ResChat chat = ChatRepository.getRepository().getChatByRoom(roomId);

        if (chat != null && chat.getEntityId() > 0) {
            // 캐시된 정보로 확인
            return chat.getCompanionId();
        } else {
            // 서버로부터 요청
            try {
                ResRoomInfo roomInfo = RequestApiManager.getInstance().getRoomInfoByRoomsApi(teamId,
                        roomId);

                if (roomInfo != null) {

                    long myId = entityManager.getMe().getId();

                    for (long member : roomInfo.getMembers()) {
                        if (myId != member) {
                            return member;
                        }
                    }

                }
            } catch (RetrofitError retrofitError) {
                retrofitError.printStackTrace();
                return -1;
            }
        }
        return -1;
    }

    private boolean isChatType(String roomType) {
        return TextUtils.equals(roomType, PushTO.RoomType.CHAT.getName());
    }

    public long getCachedLastLinkId(int roomId) {

        ResMessages.Link lastMessage = MessageRepository.getRepository().getLastMessage(roomId);

        return lastMessage.id;
    }
}
