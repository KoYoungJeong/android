package com.tosslab.jandi.app.services.socket;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.socket.JandiSocketManager;
import com.tosslab.jandi.app.network.socket.domain.ConnectTeam;
import com.tosslab.jandi.app.network.socket.events.EventListener;
import com.tosslab.jandi.app.services.SignOutService;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceStarter;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Steve SeongUg Jung on 15. 4. 3..
 */
public class JandiSocketService extends Service {

    public static final String TAG = "SocketService";
    public static final String STOP_FORCIBLY = "stop_forcibly";
    public static final String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

    private JandiSocketManager jandiSocketManager;
    private JandiSocketServiceModel jandiSocketServiceModel;
    private Map<String, EventListener> eventHashMap;

    private boolean isRegister = true;
    private boolean isStopForcibly = false;
    private boolean isRunning = false;
    private boolean isInRefreshToken = false;

    private BroadcastReceiver connectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.i(TAG, "Received connect status has changed. isRegister ? - " + isRegister);

            if (isRegister) {
                isRegister = false;
                return;
            }

            checkTokenAndTrySocketConnect();
        }
    };

    public static void stopService(Context context) {
        if (!isServiceRunning(context)) {
            return;
        }

        Intent intent = new Intent(context, JandiSocketService.class);
        intent.putExtra(STOP_FORCIBLY, true);
        context.startService(intent);
    }

    public static void startServiceIfNeed(Context context) {
        if (isServiceRunning(context)) {
            return;
        }

        Intent intent = new Intent(context, JandiSocketService.class);
        context.startService(intent);
    }

    private static boolean isServiceRunning(Context context) {ActivityManager activityManager =
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
        eventHashMap = new HashMap<String, EventListener>();

        jandiSocketServiceModel = new JandiSocketServiceModel(JandiSocketService.this);
        jandiSocketManager = JandiSocketManager.getInstance();

        IntentFilter filter = new IntentFilter(ACTION_CONNECTIVITY_CHANGE);
        registerReceiver(connectReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Crashlytics.getInstance().core.log("JandiSocketService.onStartCommand() intent is null, Flags : " + flags + ", startId : " + startId);
            stopSelf();
            return START_NOT_STICKY;
        }
        boolean isStopForcibly = intent.getBooleanExtra(STOP_FORCIBLY, false);
        LogUtil.i(TAG, "onStartCommand isRunning ? " + isRunning + " & stopForce ? " + isStopForcibly);

        setStopForcibly(isStopForcibly);
        if (isStopForcibly) {
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

    private void initEventMapper() {
        EventListener entityRefreshListener = objects -> jandiSocketServiceModel.refreshEntity(null, true);

        eventHashMap.put("team_joined", entityRefreshListener);
        eventHashMap.put("topic_created", entityRefreshListener);
        eventHashMap.put("topic_joined", entityRefreshListener);
        eventHashMap.put("topic_invite", entityRefreshListener);

        EventListener memberLeftListener = objects -> jandiSocketServiceModel.refreshLeaveMember(objects[0]);

        eventHashMap.put("team_left", memberLeftListener);

        EventListener chatLCloseListener = objects ->
                jandiSocketServiceModel.refreshChatCloseListener(objects[0]);
        eventHashMap.put("chat_close", chatLCloseListener);

        EventListener memberProfileListener = objects ->
                jandiSocketServiceModel.refreshMemberProfile(objects[0]);
        eventHashMap.put("member_profile_updated", memberProfileListener);

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

        EventListener unshareFileListener = objects ->
                jandiSocketServiceModel.unshareFile(objects[0]);
        eventHashMap.put("file_unshared", unshareFileListener);
        eventHashMap.put("file_shared", unshareFileListener);

        EventListener fileCommentRefreshListener = objects ->
                jandiSocketServiceModel.refreshFileComment(objects[0]);
        eventHashMap.put("file_comment_created", fileCommentRefreshListener);
        eventHashMap.put("file_comment_deleted", fileCommentRefreshListener);

        eventHashMap.put("check_connect_team", objects -> {
            LogUtil.d(TAG, "check_connect_team");
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
            jandiSocketManager.sendByJson("ping", "");
        });
        eventHashMap.put("pong", objects -> LogUtil.d(TAG, "pong"));
        eventHashMap.put("error_connect_team", objects -> {
            LogUtil.e(TAG, "Get Error - error_connect_team");
            sendBroadcastForRestart();
        });

        EventListener messageRefreshListener = objects ->
                jandiSocketServiceModel.refreshMessage(objects[0]);
        eventHashMap.put("message", messageRefreshListener);

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
        eventHashMap.put("announcement_created", announcementListener);
        eventHashMap.put("announcement_deleted", announcementListener);
        eventHashMap.put("announcement_status_updated", announcementListener);

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
        eventHashMap.put("folder_item_deleted", topicFolderUpdateListener);
        eventHashMap.put("folder_item_created", topicFolderUpdateListener);
        eventHashMap.put("folder_deleted", topicFolderUpdateListener);
        eventHashMap.put("folder_created", topicFolderUpdateListener);
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

        try {
            unregisterReceiver(connectReceiver);
        } catch (IllegalArgumentException e) {
            LogUtil.e("unregister receiver fail. - " + e.getMessage());
            Crashlytics.log(Log.WARN,
                    "Socket Service", "Socket Connect Receiver was unregisted : " + e.getMessage());
        }
        super.onDestroy();
        if (!isStopForcibly) {
            sendBroadcastForRestart();
        }
    }

    private void closeAll() {
        // TODO Why eventHashMap == null?
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
            }
        }).subscribeOn(Schedulers.io())
                .subscribe(isValidToken -> {
                    if (!isValidToken) {
                        LogUtil.e("Invalid Token, Stop SocketService");
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
        } catch (RetrofitError e) {
            /**
             * RefreshToken 요청에서 400 에러가 발생하면 비정상적인 RefreshToken 을 가지고 있다고 판단.
             */
            e.printStackTrace();
            if (e.getKind() == RetrofitError.Kind.HTTP
                    && e.getResponse().getStatus() == JandiConstants.NetworkError.BAD_REQUEST) {
                return false;
            }
        }
        return true;
    }

    private void trySocketConnect() {
        jandiSocketManager.connect(objects -> {
            StringBuilder sb = new StringBuilder();
            for (Object o : objects) {
                sb.append(o.toString() + "\n");
            }
            stopService(getBaseContext());
            sendBroadcastForRestart();
        });
        jandiSocketManager.register("check_connect_team", eventHashMap.get("check_connect_team"));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
