package com.tosslab.jandi.app.services.socket;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.tosslab.jandi.app.network.socket.JandiSocketManager;
import com.tosslab.jandi.app.network.socket.events.EventListener;

/**
 * Created by Steve SeongUg Jung on 15. 4. 3..
 */
public class JandiSocketService extends Service {

    private JandiSocketManager jandiSocketManager;
    private JandiSocketMonitor jandiSocketMonitor;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        jandiSocketManager = JandiSocketManager.getInstance();
        trySocketConnect();
        jandiSocketMonitor = new JandiSocketMonitor(jandiSocketManager);
        jandiSocketMonitor.startSocketMonitor();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        jandiSocketMonitor.stopSocketMonitor();
        super.onDestroy();
    }

    private void trySocketConnect() {
        if (!jandiSocketManager.isConnectingOrConnected()) {
            jandiSocketManager.connect(new EventListener() {
                @Override
                public void callback(Object... objects) {
                    Log.d("INFO", "Connect Success");
                }
            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
