package com.tosslab.jandi.app.services.socket;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.tosslab.jandi.app.network.socket.JandiSocketManager;
import com.tosslab.jandi.app.network.socket.events.EventListener;

/**
 * Created by Steve SeongUg Jung on 15. 4. 3..
 */
public class JandiSocketService extends Service {

    private JandiSocketManager jandiSocketManager;
    private JandiSocketMonitor jandiSocketMonitor;
    private JandiSocketServiceModel jandiSocketServiceModel;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        jandiSocketServiceModel = new JandiSocketServiceModel(JandiSocketService.this);
        jandiSocketManager = JandiSocketManager.getInstance();
        jandiSocketMonitor = new JandiSocketMonitor(jandiSocketManager, this::trySocketConnect);
        trySocketConnect();

        setUpSocketListener();
        return super.onStartCommand(intent, flags, startId);
    }

    private void setUpSocketListener() {
        jandiSocketManager.register("topic_created", new EventListener() {
            @Override
            public void callback(Object... objects) {

            }
        });
    }

    @Override
    public void onDestroy() {
        jandiSocketMonitor.stopSocketMonitor();
        super.onDestroy();
    }

    private void trySocketConnect() {
        if (!jandiSocketManager.isConnectingOrConnected()) {
            jandiSocketManager.connect(
                    objects -> jandiSocketManager.sendByJson("connect_team", jandiSocketServiceModel.getConnectTeam()),
                    objects -> jandiSocketManager.sendByJson("disconnect_team", jandiSocketServiceModel.getConnectTeam()));

            jandiSocketMonitor.startSocketMonitor();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
