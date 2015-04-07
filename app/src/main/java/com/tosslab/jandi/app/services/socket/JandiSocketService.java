package com.tosslab.jandi.app.services.socket;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.tosslab.jandi.app.network.socket.JandiSocketManager;
import com.tosslab.jandi.app.network.socket.events.EventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Steve SeongUg Jung on 15. 4. 3..
 */
public class JandiSocketService extends Service {

    private JandiSocketManager jandiSocketManager;
    private JandiSocketMonitor jandiSocketMonitor;
    private JandiSocketServiceModel jandiSocketServiceModel;

    private Map<String, EventListener> eventHashMap;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        jandiSocketServiceModel = new JandiSocketServiceModel(JandiSocketService.this);
        jandiSocketManager = JandiSocketManager.getInstance();
        jandiSocketMonitor = new JandiSocketMonitor(jandiSocketManager, this::trySocketConnect);

        eventHashMap = new HashMap<String, EventListener>();

        trySocketConnect();

        initEventMapper();
        setUpSocketListener();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initEventMapper() {

        EventListener entityRefreshListener = objects -> jandiSocketServiceModel.refreshEntity();

        eventHashMap.put("topic_created", entityRefreshListener);
        eventHashMap.put("topic_deleted", entityRefreshListener);
        eventHashMap.put("topic_name_updated", entityRefreshListener);
        eventHashMap.put("chat_close", entityRefreshListener);
        eventHashMap.put("team_join", entityRefreshListener);
        eventHashMap.put("member_email_updated", entityRefreshListener);
        eventHashMap.put("member_name_updated", entityRefreshListener);
        eventHashMap.put("member_profile_updated", entityRefreshListener);


        EventListener accountRefreshListener = objects -> jandiSocketServiceModel.refreshAccountInfo();

        eventHashMap.put("team_name_updated", accountRefreshListener);
        eventHashMap.put("team_domain_updated", accountRefreshListener);


        EventListener deleteFileListener = objects -> jandiSocketServiceModel.deleteFile(objects[0]);

        eventHashMap.put("file_deleted", deleteFileListener);

//        EventListener createFileListener = null;
//        eventHashMap.put("file_created", createFileListener);

        EventListener fileCommentRefreshListener = objects -> jandiSocketServiceModel.refreshFileComment(objects[0]);
        eventHashMap.put("file_comment_created", fileCommentRefreshListener);
        eventHashMap.put("file_comment_deleted", fileCommentRefreshListener);

    }

    private void setUpSocketListener() {

        for (String key : eventHashMap.keySet()) {
            jandiSocketManager.register(key, eventHashMap.get(key));
        }
    }

    @Override
    public void onDestroy() {

        for (String key : eventHashMap.keySet()) {
            jandiSocketManager.unregister(key, eventHashMap.get(key));
        }

        jandiSocketMonitor.stopSocketMonitor();
        jandiSocketManager.disconnect();
        super.onDestroy();
    }

    private void trySocketConnect() {
        if (!jandiSocketManager.isConnectingOrConnected()) {
            jandiSocketManager.connect(
                    objects -> jandiSocketManager.sendByJson("disconnect_team", jandiSocketServiceModel.getConnectTeam()));
            jandiSocketManager.register("check_connect_team", objects -> jandiSocketManager.sendByJson("connect_team", jandiSocketServiceModel.getConnectTeam()));

            jandiSocketMonitor.startSocketMonitor();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
