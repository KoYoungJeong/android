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

import com.github.nkzawa.socketio.client.Socket;
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
            trySocketConnect();
        }
    };

//    public static void startSocketServiceIfStop(Context context) {
//        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);
//
//        for (ActivityManager.RunningServiceInfo runningService : runningServices) {
//
//            if (TextUtils.equals(runningService.service.getPackageName(), context.getPackageName()) && TextUtils.equals(runningService.service.getClassName(), JandiSocketService.class.getName())) {
//                return;
//            }
//        }
//
//        context.startService(new Intent(context, JandiSocketService.class));
//    }
//

    private boolean stopForcibly = false;

    public void setStopForcibly(boolean forcibly) {
        stopForcibly = forcibly;
    }

    public static void stopSocketServiceIfRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningService : runningServices) {
            if (TextUtils.equals(runningService.service.getPackageName(), context.getPackageName()) && TextUtils.equals(runningService.service.getClassName(), JandiSocketService.class.getName())) {
                Log.e(TAG, "stopSocketServiceIfRunning");

                Intent intent = new Intent(context, JandiSocketService.class);
                intent.putExtra(STOP_FORCIBLY, true);
                context.startService(intent);
//                context.stopService(intent);
                return;
            }
        }
    }

    private boolean running = false;

    private boolean isActiveNetwork() {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand - already running ? " + running);
        boolean stopForcibly = intent.getBooleanExtra(STOP_FORCIBLY, false);
        setStopForcibly(stopForcibly);
        if (stopForcibly) {
            stopSelf();
            return START_NOT_STICKY;
        }

        jandiSocketServiceModel = new JandiSocketServiceModel(JandiSocketService.this);
        jandiSocketManager = JandiSocketManager.getInstance();

        eventHashMap = new HashMap<String, EventListener>();

        jandiSocketServiceModel.startMarkerObserver();

        initEventMapper();

        trySocketConnect();
        setUpSocketListener();

        registerReceiver(connectReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        running = true;
//        return super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    private void initEventMapper() {

        EventListener entityRefreshListener = objects -> jandiSocketServiceModel.refreshEntity();

        eventHashMap.put("team_joined", entityRefreshListener);
        eventHashMap.put("topic_created", entityRefreshListener);
        eventHashMap.put("topic_joined", entityRefreshListener);
        eventHashMap.put("topic_invite", entityRefreshListener);

        EventListener chatLCloseListener = objects -> jandiSocketServiceModel.refreshChatCloseListener(objects[0]);
        eventHashMap.put("chat_close", chatLCloseListener);

        EventListener memberProfileListener = objects -> jandiSocketServiceModel.refreshMemberProfile(objects[0]);
        eventHashMap.put("member_profile_updated", memberProfileListener);

        EventListener topicDeleteListener = objects -> jandiSocketServiceModel.refreshTopicDelete(objects[0]);
        eventHashMap.put("topic_deleted", topicDeleteListener);
        eventHashMap.put("topic_left", topicDeleteListener);

        EventListener topicStateListener = objects -> jandiSocketServiceModel.refreshTopicState(objects[0]);
        eventHashMap.put("topic_name_updated", topicStateListener);
        eventHashMap.put("topic_starred", topicStateListener);
        eventHashMap.put("topic_unstarred", topicStateListener);

        EventListener memberStarredListener = objects -> jandiSocketServiceModel.refreshMemberStarred(objects[0]);
        eventHashMap.put("member_starred", memberStarredListener);
        eventHashMap.put("member_unstarred", memberStarredListener);

        EventListener accountRefreshListener = objects -> jandiSocketServiceModel.refreshAccountInfo();
        eventHashMap.put("team_name_updated", accountRefreshListener);
        eventHashMap.put("team_domain_updated", accountRefreshListener);

        EventListener deleteFileListener = objects -> jandiSocketServiceModel.deleteFile(objects[0]);
        eventHashMap.put("file_deleted", deleteFileListener);

        EventListener createFileListener = objects -> jandiSocketServiceModel.createFile(objects[0]);
        eventHashMap.put("file_created", createFileListener);

        EventListener unshareFileListener = objects -> jandiSocketServiceModel.unshareFile(objects[0]);
        eventHashMap.put("file_unshared", unshareFileListener);
        eventHashMap.put("file_shared", unshareFileListener);

        EventListener fileCommentRefreshListener = objects -> jandiSocketServiceModel.refreshFileComment(objects[0]);
        eventHashMap.put("file_comment_created", fileCommentRefreshListener);
        eventHashMap.put("file_comment_deleted", fileCommentRefreshListener);

        eventHashMap.put("check_connect_team", objects -> {
            if (jandiSocketServiceModel.refreshToken()) {

                ConnectTeam connectTeam = jandiSocketServiceModel.getConnectTeam();
                if (connectTeam != null) {
                    Log.d(TAG, "check_connect_team - connectTeam != null");
                    jandiSocketManager.sendByJson("connect_team", connectTeam);
                } else {
//                    connectMonitor.start();
                    sendBroadcastForRestart();
                }
            } else {
//                connectMonitor.start();
                sendBroadcastForRestart();
            }
        });
        eventHashMap.put("connect_team", objects -> {
            Log.d(TAG, "connect_team");
//            connectMonitor.stop()
        });
        eventHashMap.put("error_connect_team", objects -> {
//            connectMonitor.start()
//            stopSelf();
            Log.e(TAG, "Get Error - error_connect_team");
            sendBroadcastForRestart();
        });

        EventListener messageRefreshListener = objects -> jandiSocketServiceModel.refreshMessage(objects[0]);
        eventHashMap.put("message", messageRefreshListener);

        EventListener markerUpdateListener = objects -> jandiSocketServiceModel.updateMarker(objects[0]);
        eventHashMap.put("room_marker_updated", markerUpdateListener);

//        eventHashMap.put(Socket.EVENT_DISCONNECT, objects -> Log.e(TAG, Socket.EVENT_DISCONNECT));
//        eventHashMap.put(Socket.EVENT_CONNECT, objects -> Log.e(TAG, Socket.EVENT_CONNECT));
//        eventHashMap.put(Socket.EVENT_CONNECT_ERROR, objects -> Log.e(TAG, Socket.EVENT_CONNECT_ERROR));
//        eventHashMap.put(Socket.EVENT_CONNECT_TIMEOUT, objects -> Log.e(TAG, Socket.EVENT_CONNECT_TIMEOUT));
//        eventHashMap.put(Socket.EVENT_ERROR, objects -> Log.e(TAG, Socket.EVENT_ERROR));

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

//        Intent intent = new Intent(SocketServiceBroadcastReceiver.START_SOCKET_SERVICE);
//        sendBroadcast(intent);
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
        Log.d(TAG, "onDestroy");

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
        sendBroadcast(new Intent(SocketServiceBroadcastReceiver.START_SOCKET_SERVICE));
    }

    synchronized private void trySocketConnect() {
        Log.e(TAG, "trySocketConnect");
        if (!isActiveNetwork()) {
            closeAll();
            Log.e(TAG, "can not networking");
            return;
        }

        if (!jandiSocketManager.isConnectingOrConnected()) {
            jandiSocketManager.connect(objects -> {
                StringBuilder sb = new StringBuilder();
                for (Object o : objects) {
                    sb.append(o.toString() + "\n");
                }
                stopSocketServiceIfRunning(getBaseContext());
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
