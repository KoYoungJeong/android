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
import com.tosslab.jandi.app.network.socket.domain.ConnectTeam;
import com.tosslab.jandi.app.network.socket.events.EventListener;
import com.tosslab.jandi.app.services.SignOutService;
import com.tosslab.jandi.app.services.socket.dagger.DaggerSocketServiceComponent;
import com.tosslab.jandi.app.services.socket.dagger.SocketServiceModule;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceCloser;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceStarter;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Steve SeongUg Jung on 15. 4. 3..
 */
public class JandiSocketService extends Service {

    public static final String TAG = "SocketService";
    public static final String STOP_FORCIBLY = "stop_forcibly";

    @Inject
    JandiSocketServiceModel jandiSocketServiceModel;
    private JandiSocketManager jandiSocketManager;
    private Map<String, EventListener> eventHashMap;

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
                .socketServiceModule(new SocketServiceModule(this))
                .build()
                .inject(this);

        eventHashMap = new HashMap<>();

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
            return START_NOT_STICKY;
        }

        jandiSocketServiceModel.startMarkerObserver();
        jandiSocketServiceModel.startMessageObserver();
        jandiSocketServiceModel.startLinkPreviewObserver();

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
        EventListener entityRefreshListener = objects -> jandiSocketServiceModel.refreshEntity(null, true);

        eventHashMap.put("team_joined", entityRefreshListener);
        eventHashMap.put("topic_created", entityRefreshListener);
        eventHashMap.put("topic_joined", entityRefreshListener);
        eventHashMap.put("topic_invite", entityRefreshListener);
        eventHashMap.put("member_updated", entityRefreshListener);

        EventListener memberLeftListener = objects -> jandiSocketServiceModel.refreshLeaveMember(objects[0]);
        eventHashMap.put("team_left", memberLeftListener);

        EventListener teamDeletedListener = objects -> jandiSocketServiceModel.refreshTeamDeleted(objects[0]);
        eventHashMap.put("team_deleted", teamDeletedListener);

        EventListener chatLCloseListener = objects ->
                jandiSocketServiceModel.refreshChatCloseListener(objects[0]);
        eventHashMap.put("chat_close", chatLCloseListener);

        EventListener memberProfileListener = objects ->
                jandiSocketServiceModel.refreshMemberProfile(objects[0]);
        eventHashMap.put("member_profile_updated", memberProfileListener);

        EventListener connectCreatedListener = objects -> jandiSocketServiceModel.onConnectBotCreated(objects[0]);
        eventHashMap.put("connect_created", connectCreatedListener);
        EventListener connectDeletedListener = objects -> jandiSocketServiceModel.onConnectBotDeleted(objects[0]);
        eventHashMap.put("connect_deleted", connectDeletedListener);
        EventListener connectUpdatedListener = objects -> jandiSocketServiceModel.onConnectBotUpdated(objects[0]);
        eventHashMap.put("connect_updated", connectUpdatedListener);


        EventListener topicDeleteListener = objects ->
                jandiSocketServiceModel.refreshTopicDelete(objects[0]);
        eventHashMap.put("topic_deleted", topicDeleteListener);
        eventHashMap.put("topic_left", topicDeleteListener);

        EventListener topicStateListener = objects ->
                jandiSocketServiceModel.refreshTopicState(objects[0]);
        eventHashMap.put("topic_updated", topicStateListener);
        eventHashMap.put("topic_starred", topicStateListener);
        eventHashMap.put("topic_unstarred", topicStateListener);

        EventListener memberStarredListener = objects ->
                jandiSocketServiceModel.refreshMemberStarred(objects[0]);
        eventHashMap.put("member_starred", memberStarredListener);
        eventHashMap.put("member_unstarred", memberStarredListener);

        EventListener accountRefreshListener = objects ->
                jandiSocketServiceModel.refreshAccountInfo();
        eventHashMap.put("team_name_updated", accountRefreshListener);
        eventHashMap.put("team_domain_updated", accountRefreshListener);

        EventListener deleteFileListener = objects ->
                jandiSocketServiceModel.deleteFile(objects[0]);
        eventHashMap.put("file_deleted", deleteFileListener);

        EventListener createFileListener = objects ->
                jandiSocketServiceModel.createFile(objects[0]);
        eventHashMap.put("file_created", createFileListener);

        EventListener unshareFileListener = objects -> jandiSocketServiceModel.unshareFile(objects[0]);
        eventHashMap.put("file_unshared", unshareFileListener);

        EventListener shareFileListener = objects -> jandiSocketServiceModel.shareFile(objects[0]);
        eventHashMap.put("file_shared", shareFileListener);

        EventListener fileCommentCreatedListener = objects ->
                jandiSocketServiceModel.refreshFileComment(objects[0]);
        eventHashMap.put("file_comment_created", fileCommentCreatedListener);
        EventListener fileCommentDeletedListener = objects ->
                jandiSocketServiceModel.refreshFileCommentAtTargetRoom(objects[0]);
        eventHashMap.put("file_comment_deleted", fileCommentDeletedListener);

        eventHashMap.put("check_connect_team", objects -> {
            LogUtil.d(TAG, "check_connect_team");
            JandiPreference.setSocketReconnectDelay(0l);
            ConnectTeam connectTeam = jandiSocketServiceModel.getConnectTeam();
            if (connectTeam != null) {
                jandiSocketManager.sendByJson("connect_team", connectTeam);
            } else {
                // 만료된 것으로 보고 소켓 서비스 강제 종료
                setStopForcibly(true);
                stopSelf();
            }
        });
        eventHashMap.put("connect_team", objects -> {
            LogUtil.d(TAG, "connect_team");
            jandiSocketServiceModel.updateEventHistory();
        });
        eventHashMap.put("error_connect_team", objects -> {
            LogUtil.e(TAG, "Get Error - error_connect_team");
            sendBroadcastForRestart();
        });

        // @Deprecated
        EventListener messageRefreshListener = objects ->
                jandiSocketServiceModel.refreshMessage(objects[0]);
        eventHashMap.put("message", messageRefreshListener);

        EventListener messageCreatedListener = objects ->
                jandiSocketServiceModel.createdNewMessage(objects[0]);
        eventHashMap.put("message_created", messageCreatedListener);

        EventListener messageStarredListener = objects ->
                jandiSocketServiceModel.refreshStarredMessage(objects[0]);
        eventHashMap.put("message_starred", messageStarredListener);

        EventListener messageUnstarredListener = objects ->
                jandiSocketServiceModel.refreshUnstarredMessage(objects[0]);
        eventHashMap.put("message_unstarred", messageUnstarredListener);

        EventListener markerUpdateListener = objects ->
                jandiSocketServiceModel.updateMarker(objects[0]);
        eventHashMap.put("room_marker_updated", markerUpdateListener);

        EventListener announcementListener = objects -> jandiSocketServiceModel.refreshAnnouncement(objects[0]);
        eventHashMap.put("announcement_deleted", announcementListener);
        eventHashMap.put("announcement_status_updated", announcementListener);
        EventListener announceCreatedListener = objects -> jandiSocketServiceModel.createAnnouncement(objects[0]);
        eventHashMap.put("announcement_created", announceCreatedListener);

        EventListener linkPreviewMessageUpdateListener =
                objects -> jandiSocketServiceModel.updateLinkPreviewMessage(objects[0]);
        eventHashMap.put("link_preview_created", linkPreviewMessageUpdateListener);
        EventListener linkPreviewThumbnailUpdateListener =
                objects -> jandiSocketServiceModel.updateLinkPreviewThumbnail(objects[0]);
        eventHashMap.put("link_preview_image", linkPreviewThumbnailUpdateListener);

        EventListener topicTopicPushSubscribeUpdateListener =
                objects -> jandiSocketServiceModel.updateTopicPushSubscribe(objects[0]);
        eventHashMap.put("room_subscription_updated", topicTopicPushSubscribeUpdateListener);


        EventListener topicFolderUpdateListener =
                objects -> jandiSocketServiceModel.refreshTopicFolder(objects[0]);

        eventHashMap.put("folder_updated", topicFolderUpdateListener);
        eventHashMap.put("folder_deleted", topicFolderUpdateListener);
        eventHashMap.put("folder_item_deleted", topicFolderUpdateListener);
        eventHashMap.put("folder_item_created", topicFolderUpdateListener);

        EventListener topicFolderCreated =
                objects ->jandiSocketServiceModel.onTopicFolderCreated(objects[0]);
        eventHashMap.put("folder_created", topicFolderCreated);

        EventListener topicFolderUpdated =
                objects ->jandiSocketServiceModel.onTopicFolderUpdated(objects[0]);
        eventHashMap.put("folder_updated", topicFolderUpdated);


        EventListener topicThrowOutListener =
                objects -> jandiSocketServiceModel.refreshKickedOut(objects[0]);
        eventHashMap.put("topic_kicked_out", topicThrowOutListener);
    }

    private void setUpSocketListener() {
        for (String key : eventHashMap.keySet()) {
            jandiSocketManager.register(key, eventHashMap.get(key));
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
        jandiSocketManager.disconnect();
        jandiSocketManager.release();
        jandiSocketServiceModel.stopMarkerObserver();
        jandiSocketServiceModel.stopMessageObserver();
        jandiSocketServiceModel.stopLinkPreviewObserver();
        jandiSocketServiceModel.stopRefreshEntityObserver();
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

        Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                subscriber.onNext(isValidToken());
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
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

    private Boolean isValidToken() {
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
