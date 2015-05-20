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
import android.util.Log;

import com.tosslab.jandi.app.network.socket.JandiSocketManager;
import com.tosslab.jandi.app.network.socket.domain.ConnectTeam;
import com.tosslab.jandi.app.network.socket.events.EventListener;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceBroadcastReceiver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Steve SeongUg Jung on 15. 4. 3..
 */
public class JandiSocketService extends Service {

    public static final String TAG = "SocketService";
    public static final String STOP_FORCIBLY = "stop_forcibly";
    private JandiSocketManager jandiSocketManager;
    private JandiSocketServiceModel jandiSocketServiceModel;
    private Map<String, EventListener> eventHashMap;

    private BroadcastReceiver connectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "receive connect status has changed. register ? - " + register);
//            if (intent != null) {
//                String action = intent.getAction();
//                if ("register".equals(action)) {
//                    Log.d(TAG, "register flow");
//                    return;
//                }
//            }
            if (register) {
                register = false;
                return;
            }

            trySocketConnect();
        }
    };

    private boolean register = true;

    private boolean stopForcibly = false;

    public void setStopForcibly(boolean forcibly) {
        stopForcibly = forcibly;
    }

    public static void startSocketServiceIfNeed(Context context) {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices =
                activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningService : runningServices) {
            String packageName = runningService.service.getPackageName();
            String className = runningService.service.getClassName();
            if (TextUtils.equals(packageName, context.getPackageName())
                    && TextUtils.equals(className, JandiSocketService.class.getName())) {
                Log.e(TAG, "startSocketServiceIfNeed");

                Intent intent = new Intent(context, JandiSocketService.class);
                intent.putExtra(STOP_FORCIBLY, true);
                context.startService(intent);
                return;
            }
        }
    }

    private boolean running = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand - already running ? " + running);
        boolean stopForcibly = intent.getBooleanExtra(STOP_FORCIBLY, false);
        setStopForcibly(stopForcibly);
        if (stopForcibly) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (running) {
            return START_NOT_STICKY;
        }

        jandiSocketServiceModel = new JandiSocketServiceModel(JandiSocketService.this);
        jandiSocketManager = JandiSocketManager.getInstance();

        eventHashMap = new HashMap<String, EventListener>();

        jandiSocketServiceModel.startMarkerObserver();

        initEventMapper();

        trySocketConnect();
        setUpSocketListener();

        running = true;

        IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
//        filter.addAction("register");
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
            if (jandiSocketServiceModel.refreshToken()) {
                ConnectTeam connectTeam = jandiSocketServiceModel.getConnectTeam();
                if (connectTeam != null) {
                    Log.d(TAG, "check_connect_team - connectTeam != null");
                    jandiSocketManager.sendByJson("connect_team", connectTeam);
                } else {
                    sendBroadcastForRestart();
                }
            } else {
                sendBroadcastForRestart();
            }
        });
        eventHashMap.put("connect_team", objects -> {
            Log.d(TAG, "connect_team");
        });
        eventHashMap.put("error_connect_team", objects -> {
            Log.e(TAG, "Get Error - error_connect_team");
            sendBroadcastForRestart();
        });

        EventListener messageRefreshListener = objects ->
                jandiSocketServiceModel.refreshMessage(objects[0]);
        eventHashMap.put("message", messageRefreshListener);

        EventListener markerUpdateListener = objects ->
                jandiSocketServiceModel.updateMarker(objects[0]);
        eventHashMap.put("room_marker_updated", markerUpdateListener);
    }

    private void setUpSocketListener() {
        for (String key : eventHashMap.keySet()) {
            jandiSocketManager.register(key, eventHashMap.get(key));
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e(TAG, "onTaskRemoved - " + jandiSocketManager.isConnectingOrConnected());
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.e(TAG, "onTrimMemory - " + level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.e(TAG, "onLowMemory");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy forcibly stop ? " + stopForcibly);

        closeAll();

        running = false;
        unregisterReceiver(connectReceiver);
        super.onDestroy();
        if (!stopForcibly) {
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
        Log.e(TAG, "sendBroadcastForRestart");
        sendBroadcast(new Intent(SocketServiceBroadcastReceiver.START_SOCKET_SERVICE));
    }

    private boolean isActiveNetwork() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    synchronized private void trySocketConnect() {
        if (!isActiveNetwork()) {
            Log.e(TAG, "can not networking");
            closeAll();
            return;
        }

        if (!jandiSocketManager.isConnectingOrConnected()) {
            Log.e(TAG, "trySocketConnect");
            jandiSocketManager.connect(objects -> {
                StringBuilder sb = new StringBuilder();
                for (Object o : objects) {
                    sb.append(o.toString() + "\n");
                }
                startSocketServiceIfNeed(getBaseContext());
                sendBroadcastForRestart();
            });
            jandiSocketManager.register("check_connect_team", eventHashMap.get("check_connect_team"));
        } else {
            Log.e(TAG, "socket is connected");
            setUpSocketListener();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
