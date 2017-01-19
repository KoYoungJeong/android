package com.tosslab.jandi.app.services.socket;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.socket.JandiSocketManager;
import com.tosslab.jandi.app.network.socket.domain.SocketStart;
import com.tosslab.jandi.app.network.socket.domain.SocketUpdateMember;
import com.tosslab.jandi.app.network.socket.domain.SocketUpdateRoom;
import com.tosslab.jandi.app.network.socket.events.EventListener;
import com.tosslab.jandi.app.services.SignOutService;
import com.tosslab.jandi.app.services.socket.dagger.DaggerSocketServiceComponent;
import com.tosslab.jandi.app.services.socket.model.SocketEmitModel;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceCloser;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceStarter;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.schedulers.Schedulers;

public class JandiSocketService extends Service {

    public static final String TAG = "SocketService";
    public static final String STOP_FORCIBLY = "stop_forcibly";

    @Inject
    JandiSocketServiceModel jandiSocketServiceModel;
    private JandiSocketManager jandiSocketManager;
    private Map<String, EventListener> eventHashMap;
    private Map<String, List<EventListener>> eventsHashMap;

    private boolean isStopForcibly = false;
    private boolean isRunning = false;
    private boolean isInRefreshToken = false;

    public static void stopService(Context context) {
        if (!isServiceRunning(context)) {
            return;
        }

        Intent intent = new Intent(context, JandiSocketService.class);
        intent.putExtra(STOP_FORCIBLY, true);
        context.startService(intent);

        SocketServiceCloser.getInstance().cancel();
    }

    public static void startServiceIfNeed(Context context) {
        SocketServiceCloser.getInstance().cancel();

        if (isServiceRunning(context)) {
            return;
        }

        Intent intent = new Intent(context, JandiSocketService.class);
        context.startService(intent);
    }

    /**
     * use to be careful!!!
     *
     * @param context
     */
    public static void startServiceForcily(Context context) {
        SocketServiceCloser.getInstance().cancel();

        Intent intent = new Intent(context, JandiSocketService.class);
        context.startService(intent);
    }

    public static boolean isServiceRunning(Context context) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices =
                activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo runningService : runningServices) {
            String packageName = runningService.service.getPackageName();
            String className = runningService.service.getClassName();
            if (TextUtils.equals(packageName, context.getPackageName())
                    && TextUtils.equals(className, JandiSocketService.class.getName())) {
                LogUtil.e(TAG, "Service is running.");
                return true;
            }
        }
        return false;
    }

    public void setStopForcibly(boolean forcibly) {
        isStopForcibly = forcibly;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DaggerSocketServiceComponent.builder()
                .build()
                .inject(this);

        eventHashMap = new HashMap<>();
        eventsHashMap = new HashMap<>();

        jandiSocketManager = JandiSocketManager.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            logForCrashlytics(flags, startId);
            stopSelf();
            return START_NOT_STICKY;
        }

        boolean isApplicationDeactive = JandiApplication.isApplicationDeactive();
        boolean isStopForcibly = intent.getBooleanExtra(STOP_FORCIBLY, false);

        logForServiceStartChecking(isApplicationDeactive, isStopForcibly);

        boolean forceStop = isApplicationDeactive || isStopForcibly;
        setStopForcibly(forceStop);
        if (forceStop) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (isRunning) {
            checkTokenAndTrySocketConnect();
            return START_NOT_STICKY;
        }

        jandiSocketServiceModel.startMarkerObserver();

        initEventMapper();
        setUpSocketListener();

        checkTokenAndTrySocketConnect();

        isRunning = true;
        return START_NOT_STICKY;
    }

    private void logForServiceStartChecking(boolean isApplicationDeactive, boolean isStopForcibly) {
        String format = "onStartCommand isRunning ? %b & isApplicationDeactive ? %b, stopForce ? %b";
        String log = String.format(format, isRunning, isApplicationDeactive, isStopForcibly);
        LogUtil.i(TAG, log);
    }

    private void logForCrashlytics(int flags, int startId) {
        String format = "JandiSocketService.onStartCommand() intent is null, Flags : %d, startId : %d";
        String log = String.format(format, flags, startId);
        Crashlytics.getInstance().core.log(log);
    }


    private void initEventMapper() {

        EventListener teamCreateListener = objects -> jandiSocketServiceModel.onTeamCreated(objects[0]);
        EventListener teamCreateUpdateListener = objects -> {
            SocketUpdateMember socketUpdateMember = SocketEmitModel.teamCreated(objects[0]);
            if (socketUpdateMember != null) {
                jandiSocketManager.sendByJson("update_membership", socketUpdateMember);
            }
        };
        eventsHashMap.put("team_created", Arrays.asList(teamCreateUpdateListener, teamCreateListener));

        EventListener teamJoinListener = objects -> jandiSocketServiceModel.onTeamJoin(objects[0]);
        EventListener teamJoinUpdateListener = objects -> {
            SocketUpdateMember socketUpdateMember = SocketEmitModel.teamJoin(objects[0]);
            if (socketUpdateMember != null) {
                jandiSocketManager.sendByJson("update_membership", socketUpdateMember);
            }
        };
        eventsHashMap.put("team_joined", Arrays.asList(teamJoinUpdateListener, teamJoinListener));

        EventListener memberLeftListener = objects -> jandiSocketServiceModel.onTeamLeft(objects[0]);
        EventListener memberLeftUpdateListener = objects -> {
            SocketUpdateMember socketUpdateMember = SocketEmitModel.teamLeft(objects[0]);
            if (socketUpdateMember != null) {
                jandiSocketManager.sendByJson("update_membership", socketUpdateMember);
            }

        };

        eventsHashMap.put("team_left", Arrays.asList(memberLeftUpdateListener, memberLeftListener));
        EventListener teamDeletedListener = objects -> jandiSocketServiceModel.onTeamDeleted(objects[0]);
        EventListener teamDeletedUpdateListener = objects -> {
            SocketUpdateMember socketUpdateMember = SocketEmitModel.teamDeleted(objects[0]);
            if (socketUpdateMember != null) {
                jandiSocketManager.sendByJson("update_membership", socketUpdateMember);
            }

        };

        eventsHashMap.put("team_deleted", Arrays.asList(teamDeletedUpdateListener, teamDeletedListener));

        EventListener teamUpdatedListener = objects -> jandiSocketServiceModel.onTeamUpdated(objects[0]);
        eventHashMap.put("team_updated", teamUpdatedListener);


        EventListener chatLCloseListener = objects -> jandiSocketServiceModel.onChatClosed(objects[0]);
        eventHashMap.put("chat_closed", chatLCloseListener);

        EventListener chatCreatedListener = objects -> jandiSocketServiceModel.onChatCreated(objects[0]);
        eventHashMap.put("chat_created", chatCreatedListener);

        EventListener connectCreatedListener = objects -> jandiSocketServiceModel.onConnectBotCreated(objects[0]);
        eventHashMap.put("connect_created", connectCreatedListener);
        EventListener connectDeletedListener = objects -> jandiSocketServiceModel.onConnectBotDeleted(objects[0]);
        eventHashMap.put("connect_deleted", connectDeletedListener);
        EventListener connectUpdatedListener = objects -> jandiSocketServiceModel.onConnectBotUpdated(objects[0]);
        eventHashMap.put("connect_updated", connectUpdatedListener);

        EventListener topicLeftListener = objects -> jandiSocketServiceModel.onTopicLeft(objects[0]);
        EventListener topicLeftUpdateListener = objects -> {
            SocketUpdateRoom socketUpdateRoom = SocketEmitModel.topicLeft(objects[0]);
            if (socketUpdateRoom != null) {
                jandiSocketManager.sendByJson("update_room", socketUpdateRoom);
            }
        };
        eventsHashMap.put("topic_left", Arrays.asList(topicLeftUpdateListener, topicLeftListener));
        EventListener topicDeletedListener = objects -> jandiSocketServiceModel.onTopicDeleted(objects[0]);
        EventListener topicDeletedUpdateListener = objects -> {
            SocketUpdateRoom socketUpdateRoom = SocketEmitModel.topicDelete(objects[0]);
            if (socketUpdateRoom != null) {
                jandiSocketManager.sendByJson("update_room", socketUpdateRoom);
            }
        };
        eventsHashMap.put("topic_deleted", Arrays.asList(topicDeletedUpdateListener, topicDeletedListener));
        EventListener teamCreatedListener = objects -> jandiSocketServiceModel.onTopicCreated(objects[0]);
        EventListener teamCreatedUpdateListener = objects -> {
            SocketUpdateRoom socketUpdateRoom = SocketEmitModel.topicCreate(objects[0]);
            if (socketUpdateRoom != null) {
                jandiSocketManager.sendByJson("update_room", socketUpdateRoom);
            }
        };
        eventsHashMap.put("topic_created", Arrays.asList(teamCreatedUpdateListener, teamCreatedListener));
        EventListener topicJoinListener = objects -> jandiSocketServiceModel.onTopicJoined(objects[0]);
        EventListener topicJoinUpdateListener = objects -> {
            SocketUpdateRoom socketUpdateRoom = SocketEmitModel.topicJoin(objects[0]);
            if (socketUpdateRoom != null) {
                jandiSocketManager.sendByJson("update_room", socketUpdateRoom);
            }
        };
        eventsHashMap.put("topic_joined", Arrays.asList(topicJoinUpdateListener, topicJoinListener));
        EventListener topicInviteListener = objects -> jandiSocketServiceModel.onTopicInvited(objects[0]);
        eventHashMap.put("topic_invited", topicInviteListener);
        EventListener topicUpdatedListener = objects -> jandiSocketServiceModel.onTopicUpdated(objects[0]);
        eventHashMap.put("topic_updated", topicUpdatedListener);
        EventListener topicStarredListener = objects -> jandiSocketServiceModel.onTopicStarred(objects[0]);
        eventHashMap.put("topic_starred", topicStarredListener);
        EventListener topicUnstarredListener = objects -> jandiSocketServiceModel.onTopicUnstarred(objects[0]);
        eventHashMap.put("topic_unstarred", topicUnstarredListener);
        EventListener topicThrowOutListener = objects -> jandiSocketServiceModel.onTopicKickOut(objects[0]);
        eventHashMap.put("topic_kicked_out", topicThrowOutListener);

        EventListener memberStarredListener = objects -> jandiSocketServiceModel.onMemberStarred(objects[0]);
        eventHashMap.put("member_starred", memberStarredListener);
        EventListener memberUnstarredListener = objects -> jandiSocketServiceModel.onMemberUnstarred(objects[0]);
        eventHashMap.put("member_unstarred", memberUnstarredListener);
        EventListener memberUpdatedListener = objects -> jandiSocketServiceModel.onMemberUpdated(objects[0]);
        eventHashMap.put("member_updated", memberUpdatedListener);
        EventListener memberRankUpdateListener = objects -> jandiSocketServiceModel.onMemberRankUpdated(objects[0]);
        eventHashMap.put("member_rank_updated", memberRankUpdateListener);

        EventListener fileCreatedListener = objects -> jandiSocketServiceModel.onFileCreated(objects[0]);
        eventHashMap.put("file_created", fileCreatedListener);

        EventListener fileDeletedListener = objects -> jandiSocketServiceModel.onFileDeleted(objects[0]);
        eventHashMap.put("file_deleted", fileDeletedListener);

        EventListener fileUnsharedListener = objects -> jandiSocketServiceModel.onFileUnshared(objects[0]);
        eventHashMap.put("file_unshared", fileUnsharedListener);

        EventListener fileSharedListener = objects -> jandiSocketServiceModel.onFileShared(objects[0]);
        eventHashMap.put("file_shared", fileSharedListener);


        EventListener fileCommentCreatedListener = objects ->
                jandiSocketServiceModel.onFileCommentCreated(objects[0]);
        eventHashMap.put("file_comment_created", fileCommentCreatedListener);
        EventListener fileCommentDeletedListener = objects ->
                jandiSocketServiceModel.onFileCommentDeleted(objects[0]);
        eventHashMap.put("file_comment_deleted", fileCommentDeletedListener);

        EventListener messageRefreshListener = objects ->
                jandiSocketServiceModel.onMessageDeleted(objects[0]);
        eventHashMap.put("message_deleted", messageRefreshListener);
        EventListener messageCreatedListener = objects ->
                jandiSocketServiceModel.onMessageCreated(objects[0], false);
        eventHashMap.put("message_created", messageCreatedListener);

        EventListener messageStarredListener = objects ->
                jandiSocketServiceModel.onMessageStarred(objects[0]);
        eventHashMap.put("message_starred", messageStarredListener);

        EventListener messageUnstarredListener = objects ->
                jandiSocketServiceModel.onMessageUnstarred(objects[0]);
        eventHashMap.put("message_unstarred", messageUnstarredListener);

        EventListener markerUpdateListener = objects ->
                jandiSocketServiceModel.onRoomMarkerUpdated(objects[0]);
        eventHashMap.put("room_marker_updated", markerUpdateListener);

        EventListener announcementDeletedListener = objects -> jandiSocketServiceModel.onAnnouncementDeleted(objects[0]);
        eventHashMap.put("announcement_deleted", announcementDeletedListener);

        EventListener announcementStatusUpdatedListener = objects -> jandiSocketServiceModel.onAnnouncementStatusUpdated(objects[0]);
        eventHashMap.put("announcement_status_updated", announcementStatusUpdatedListener);
        EventListener announceCreatedListener = objects -> jandiSocketServiceModel.onAnnouncementCreated(objects[0]);
        eventHashMap.put("announcement_created", announceCreatedListener);

        EventListener linkPreviewMessageUpdateListener =
                objects -> jandiSocketServiceModel.onLinkPreviewCreated(objects[0]);
        eventHashMap.put("link_preview_created", linkPreviewMessageUpdateListener);
        EventListener linkPreviewThumbnailUpdateListener =
                objects -> jandiSocketServiceModel.onLinkPreviewImage(objects[0]);
        eventHashMap.put("link_preview_image", linkPreviewThumbnailUpdateListener);

        EventListener topicTopicPushSubscribeUpdateListener =
                objects -> jandiSocketServiceModel.onRoomSubscriptionUpdated(objects[0]);
        eventHashMap.put("room_subscription_updated", topicTopicPushSubscribeUpdateListener);


        EventListener topicFolderCreated =
                objects -> jandiSocketServiceModel.onTopicFolderCreated(objects[0]);
        eventHashMap.put("folder_created", topicFolderCreated);
        EventListener topicFolderUpdated =
                objects -> jandiSocketServiceModel.onTopicFolderUpdated(objects[0]);
        eventHashMap.put("folder_updated", topicFolderUpdated);
        EventListener folderDeletedListener =
                objects -> jandiSocketServiceModel.onFolderDeleted(objects[0]);
        eventHashMap.put("folder_deleted", folderDeletedListener);
        EventListener folderItemCreatedListener =
                objects -> jandiSocketServiceModel.onFolderItemCreated(objects[0]);
        eventHashMap.put("folder_item_created", folderItemCreatedListener);
        EventListener folderItemDeletedListener =
                objects -> jandiSocketServiceModel.onFolderItemDeleted(objects[0]);
        eventHashMap.put("folder_item_deleted", folderItemDeletedListener);

        EventListener pollCreatedListener =
                objects -> jandiSocketServiceModel.onPollCreated(objects[0]);
        eventHashMap.put("poll_created", pollCreatedListener);
        EventListener pollFinishedListener =
                objects -> jandiSocketServiceModel.onPollFinished(objects[0]);
        eventHashMap.put("poll_finished", pollFinishedListener);
        EventListener pollDeletedListener =
                objects -> jandiSocketServiceModel.onPollDeleted(objects[0]);
        eventHashMap.put("poll_deleted", pollDeletedListener);
        EventListener pollVotedListener =
                objects -> jandiSocketServiceModel.onPollVoted(objects[0]);
        eventHashMap.put("poll_voted", pollVotedListener);

        EventListener pollCommentCreatedListener =
                objects -> jandiSocketServiceModel.onPollCommentCreated(objects[0]);
        eventHashMap.put("poll_comment_created", pollCommentCreatedListener);
        EventListener pollCommentDeletedListener =
                objects -> jandiSocketServiceModel.onPollCommentDeleted(objects[0]);
        eventHashMap.put("poll_comment_deleted", pollCommentDeletedListener);
        EventListener mentionMarkerUpdatedListener =
                objects -> jandiSocketServiceModel.onMentionMarkerUpdated(objects[0]);
        eventHashMap.put("mention_marker_updated", mentionMarkerUpdatedListener);

        eventHashMap.put("ready_to_start", objects -> {
            JandiPreference.setSocketReconnectDelay(0l);
            SocketStart socketStart = jandiSocketServiceModel.getStartInfo();
            if (socketStart != null) {
                jandiSocketManager.sendByJson("start", socketStart);
            } else {
                // 만료된 것으로 보고 소켓 서비스 강제 종료
                setStopForcibly(true);
                stopSelf();
            }
        });
        eventHashMap.put("start", objects -> {
            jandiSocketManager.sendByJson("jandi_ping", "");
            jandiSocketServiceModel.updateEventHistory();
        });

        eventHashMap.put("jandi_ping", objects -> {
        });

        eventHashMap.put("error_start", objects -> {
            LogUtil.e(TAG, "Get Error - error_connect_team");
            sendBroadcastForRestart();
        });

    }

    private void setUpSocketListener() {
        for (String key : eventHashMap.keySet()) {
            jandiSocketManager.register(key, eventHashMap.get(key));
        }

        for (String key : eventsHashMap.keySet()) {
            for (EventListener eventListener : eventsHashMap.get(key)) {
                jandiSocketManager.register(key, eventListener);
            }
        }
    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG, "onDestroy forcibly stop ? " + isStopForcibly);

        closeAll();

        isRunning = false;

        super.onDestroy();
        if (!isStopForcibly && !JandiApplication.isApplicationDeactive()) {
            sendBroadcastForRestart();
        }
    }

    private void closeAll() {
        // FIXME Why eventHashMap == null?
        if (eventHashMap != null) {
            for (String key : eventHashMap.keySet()) {
                jandiSocketManager.unregister(key, eventHashMap.get(key));
            }
        }

        if (eventsHashMap != null) {
            for (String key : eventsHashMap.keySet()) {
                for (EventListener eventListener : eventsHashMap.get(key)) {
                    jandiSocketManager.unregister(key, eventListener);
                }
            }
        }

        jandiSocketManager.disconnect();
        jandiSocketManager.release();
        jandiSocketServiceModel.stopMarkerObserver();
        jandiSocketServiceModel.stopEventPublisher();
    }

    private void sendBroadcastForRestart() {
        sendBroadcast(new Intent(SocketServiceStarter.START_SOCKET_SERVICE));
    }

    private boolean isActiveNetwork() {
        return NetworkCheckUtil.isConnected();
    }

    private synchronized void checkTokenAndTrySocketConnect() {
        LogUtil.e(TAG, "checkTokenAndTrySocketConnect");

        if (!isActiveNetwork()) {
            LogUtil.e(TAG, "Unavailable networking");
            closeAll();
            return;
        }

        if (jandiSocketManager.isConnectingOrConnected()) {
            LogUtil.d(TAG, "Socket is connected");
            setUpSocketListener();
            return;
        }

        if (isInRefreshToken) {
            return;
        }

        isInRefreshToken = true;

        Observable.defer(() -> Observable.just(isValidToken()))
                .subscribeOn(Schedulers.io())
                .subscribe(isValidToken -> {
                    if (!isValidToken) {
                        LogUtil.e(TAG, "Invalid Token, Stop SocketService");
                        setStopForcibly(true);
                        stopSelf();
                        SignOutService.start();
                        return;
                    }

                    trySocketConnect();
                });
    }

    private boolean isValidToken() {
        try {
            jandiSocketServiceModel.refreshToken();
        } catch (RetrofitException e) {
            /**
             * RefreshToken 요청에서 400 에러가 발생하면 비정상적인 RefreshToken 을 가지고 있다고 판단.
             */
            e.printStackTrace();
            if (e.getStatusCode() == JandiConstants.NetworkError.BAD_REQUEST) {
                return false;
            }
        }
        return true;
    }

    private void trySocketConnect() {
        if (JandiPreference.getSocketReconnectDelay() > 0) {
            try {
                Thread.sleep(JandiPreference.getSocketReconnectDelay());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        jandiSocketManager.connect(objects -> {
            StringBuilder sb = new StringBuilder();
            for (Object o : objects) {
                sb.append(o.toString() + "\n");
            }
            LogUtil.e(TAG, "Socket disconnected. " + sb.toString());
            long socketReconnectDelay = JandiPreference.getSocketReconnectDelay();
            if (socketReconnectDelay == 0) {
                JandiPreference.setSocketReconnectDelay(1000l);
            } else if (socketReconnectDelay * 2 > 1000 * 60 * 5) {
                JandiPreference.setSocketReconnectDelay(0l);
            } else {
                JandiPreference.setSocketReconnectDelay(socketReconnectDelay * 2);
            }
            stopService(getBaseContext());

            if (isActiveNetwork()) {
                sendBroadcastForRestart();
            }

        });
        jandiSocketManager.register("check_connect_team", eventHashMap.get("check_connect_team"));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
