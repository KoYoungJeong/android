package com.tosslab.jandi.app.services.socket;

import com.tosslab.jandi.app.network.socket.JandiSocketManager;

/**
 * Created by Steve SeongUg Jung on 15. 4. 6..
 */
public class JandiSocketMonitor implements Runnable {
    private JandiSocketManager jandiSocketManager;

    private boolean monitor;
    private Thread monitorThread;

    public JandiSocketMonitor(JandiSocketManager jandiSocketManager) {
        this.jandiSocketManager = jandiSocketManager;
        monitor = true;
    }

    public void stopSocketMonitor() {
        monitor = false;
        monitorThread = null;
    }

    public void startSocketMonitor() {
        if (!monitor && monitorThread != null) {
            monitor = true;
            monitorThread = new Thread(this);
            monitorThread.start();
        }
    }

    @Override
    public void run() {

        while (monitor) {

            long sleepTime = getSleepTime(jandiSocketManager);

            if (!jandiSocketManager.isConnectingOrConnected()) {
                // TODO Callback for Reconnect
            }

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    private long getSleepTime(JandiSocketManager jandiSocketManager) {

        long sleepTime;

        if (jandiSocketManager.isConnectingOrConnected()) {
            sleepTime = 1000 * 60 * 30; // 30 min
        } else {
            sleepTime = 1000 * 60 * 3;  //  3 min
        }

        return sleepTime;
    }

}
