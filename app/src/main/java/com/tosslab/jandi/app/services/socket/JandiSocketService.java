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
import com.tosslab.jandi.app.network.socket.events.EventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Steve SeongUg Jung on 15. 4. 3..
 */
public class JandiSocketService extends Service {

    private JandiSocketManager jandiSocketManager;
    private BroadcastReceiver connectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("INFO", "connectReceiver : onReceive");
            if (isActiveNetwork()) {
                trySocketConnect();
            }
        }
    };
    private JandiSocketServiceModel jandiSocketServiceModel;
    private Map<String, EventListener> eventHashMap;

    public static void startSocketServiceIfStop(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningService : runningServices) {
            if (TextUtils.equals(runningService.service.getClassName(), JandiSocketService.class.getName())) {
                return;
            }
        }

        context.startService(new Intent(context, JandiSocketService.class));
    }

    private boolean isActiveNetwork() {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        jandiSocketServiceModel = new JandiSocketServiceModel(JandiSocketService.this);
        jandiSocketManager = JandiSocketManager.getInstance();

        eventHashMap = new HashMap<String, EventListener>();

        initEventMapper();

        trySocketConnect();
        setUpSocketListener();

        registerReceiver(connectReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        return super.onStartCommand(intent, flags, startId);
    }

    private void initEventMapper() {

        EventListener entityRefreshListener = objects -> jandiSocketServiceModel.refreshEntity();

        eventHashMap.put("team_join", entityRefreshListener);
        eventHashMap.put("topic_created", entityRefreshListener);
        eventHashMap.put("topic_deleted", entityRefreshListener);
        eventHashMap.put("topic_name_updated", entityRefreshListener);
        eventHashMap.put("topic_starred", entityRefreshListener);
        eventHashMap.put("topic_unstarred", entityRefreshListener);
        eventHashMap.put("chat_close", entityRefreshListener);
        eventHashMap.put("member_email_updated", entityRefreshListener);
        eventHashMap.put("member_name_updated", entityRefreshListener);
        eventHashMap.put("member_profile_updated", entityRefreshListener);
        eventHashMap.put("member_starred", entityRefreshListener);
        eventHashMap.put("member_unstarred", entityRefreshListener);


        EventListener accountRefreshListener = objects -> jandiSocketServiceModel.refreshAccountInfo();

        eventHashMap.put("team_name_updated", accountRefreshListener);
        eventHashMap.put("team_domain_updated", accountRefreshListener);

        EventListener deleteFileListener = objects -> jandiSocketServiceModel.deleteFile(objects[0]);

        eventHashMap.put("file_deleted", deleteFileListener);

        EventListener fileCommentRefreshListener = objects -> jandiSocketServiceModel.refreshFileComment(objects[0]);
        eventHashMap.put("file_comment_created", fileCommentRefreshListener);
        eventHashMap.put("file_comment_deleted", fileCommentRefreshListener);

        eventHashMap.put("check_connect_team", objects -> jandiSocketManager.sendByJson("connect_team", jandiSocketServiceModel.getConnectTeam()));

    }

    private void setUpSocketListener() {

        for (String key : eventHashMap.keySet()) {
            jandiSocketManager.register(key, eventHashMap.get(key));
        }
    }

    @Override
    public void onDestroy() {

        unregisterReceiver(connectReceiver);

        for (String key : eventHashMap.keySet()) {
            jandiSocketManager.unregister(key, eventHashMap.get(key));
        }

        Log.d("INFO", "onDestroy stopSocketMonitor");
        jandiSocketManager.disconnect();
        super.onDestroy();
    }

    private void trySocketConnect() {
        Log.d("INFO", "trySocketConnect Start");

        if (!isActiveNetwork()) {
            return;
        }

        if (!jandiSocketManager.isConnectingOrConnected()) {
            Log.d("INFO", "trySocketConnect not Connected");
            jandiSocketManager.connect(objects -> {
                Log.d("INFO", "Disconnected");
                if (isActiveNetwork()) {
                    trySocketConnect();
                    setUpSocketListener();
                }
            });

            jandiSocketManager.register("check_connect_team", eventHashMap.get("check_connect_team"));
        } else {
            Log.d("INFO", "trySocketConnect is Connected");
            setUpSocketListener();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
