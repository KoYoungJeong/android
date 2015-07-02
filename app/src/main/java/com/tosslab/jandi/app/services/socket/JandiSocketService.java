package com.tosslab.jandi.app.services.socket;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.text.TextUtils;

import com.tosslab.jandi.app.network.socket.JandiSocketManager;
import com.tosslab.jandi.app.network.socket.domain.ConnectTeam;
import com.tosslab.jandi.app.network.socket.events.EventListener;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceStarter;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private BroadcastReceiver connectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.e(TAG, "Received connect status has changed. isRegister ? - " + isRegister);
            if (isRegister) {
                isRegister = false;
                return;
            }

            trySocketConnect();
        }
    };

    public static void stopService(Context context) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices =
                activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningService : runningServices) {
            String packageName = runningService.service.getPackageName();
            String className = runningService.service.getClassName();
            if (TextUtils.equals(packageName, context.getPackageName())
                    && TextUtils.equals(className, JandiSocketService.class.getName())) {
                LogUtil.e(TAG, "stopService");

                Intent intent = new Intent(context, JandiSocketService.class);
                intent.putExtra(STOP_FORCIBLY, true);
                context.startService(intent);
                return;
            }
        }
    }

    public void setStopForcibly(boolean forcibly) {
        isStopForcibly = forcibly;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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

        jandiSocketServiceModel = new JandiSocketServiceModel(JandiSocketService.this);
        jandiSocketManager = JandiSocketManager.getInstance();

        eventHashMap = new HashMap<String, EventListener>();

        jandiSocketServiceModel.startMarkerObserver();

        initEventMapper();

        trySocketConnect();
        setUpSocketListener();

        isRunning = true;

        IntentFilter filter = new IntentFilter(ACTION_CONNECTIVITY_CHANGE);
        registerReceiver(connectReceiver, filter);
        return START_NOT_STICKY;
    }

    private void initEventMapper() {
        EventListener entityRefreshListener = objects -> jandiSocketServiceModel.refreshEntity();

        eventHashMap.put("team_joined", entityRefreshListener);
        eventHashMap.put("topic_created", entityRefreshListener);
        eventHashMap.put("topic_joined", entityRefreshListener);
        eventHashMap.put("topic_invite", entityRefreshListener);

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
        eventHashMap.put("topic_name_updated", topicStateListener);
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

            long last = System.currentTimeMillis();
            LogUtil.i(TAG, "start time = " + last);
            boolean refreshToken = jandiSocketServiceModel.refreshToken();
            long current = System.currentTimeMillis();

            LogUtil.i(TAG, "end time - " + current + " gap = " + (current - last));

            if (refreshToken) {
                ConnectTeam connectTeam = jandiSocketServiceModel.getConnectTeam();
                if (connectTeam != null) {
                    jandiSocketManager.sendByJson("connect_team", connectTeam);
                } else {
                    stopSelf();
                }
            } else {
                LogUtil.e(TAG, "refreshToken failed");
                stopSelf();
            }
        });
        eventHashMap.put("connect_team", objects -> {
            LogUtil.d(TAG, "connect_team");
        });
        eventHashMap.put("error_connect_team", objects -> {
            LogUtil.e(TAG, "Get Error - error_connect_team");
            sendBroadcastForRestart();
        });

        EventListener messageRefreshListener = objects ->
                jandiSocketServiceModel.refreshMessage(objects[0]);
        eventHashMap.put("message", messageRefreshListener);

        EventListener markerUpdateListener = objects ->
                jandiSocketServiceModel.updateMarker(objects[0]);
        eventHashMap.put("room_marker_updated", markerUpdateListener);

        EventListener announcementListener = objects -> jandiSocketServiceModel.refreshAnnouncement(objects[0]);
        eventHashMap.put("announcement_created", announcementListener);
        eventHashMap.put("announcement_deleted", announcementListener);
        eventHashMap.put("announcement_status_updated", announcementListener);

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
        unregisterReceiver(connectReceiver);
        super.onDestroy();
        if (!isStopForcibly) {
            sendBroadcastForRestart();
        }
    }

    private void closeAll() {
        for (String key : eventHashMap.keySet()) {
            jandiSocketManager.unregister(key, eventHashMap.get(key));
        }

        jandiSocketManager.disconnect();
        jandiSocketManager.release();
        jandiSocketServiceModel.stopMarkerObserver();
    }

    private void sendBroadcastForRestart() {
        sendBroadcast(new Intent(SocketServiceStarter.START_SOCKET_SERVICE));
    }

    private boolean isActiveNetwork() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    synchronized private void trySocketConnect() {
        if (!isActiveNetwork()) {
            LogUtil.e(TAG, "Unavailable networking");
            closeAll();
            return;
        }

        if (!jandiSocketManager.isConnectingOrConnected()) {
            LogUtil.e(TAG, "trySocketConnect");
            jandiSocketManager.connect(objects -> {
                StringBuilder sb = new StringBuilder();
                for (Object o : objects) {
                    sb.append(o.toString() + "\n");
                }
                stopService(getBaseContext());
                sendBroadcastForRestart();
            });
            jandiSocketManager.register("check_connect_team", eventHashMap.get("check_connect_team"));
        } else {
            LogUtil.d(TAG, "Socket is connected");
            setUpSocketListener();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
