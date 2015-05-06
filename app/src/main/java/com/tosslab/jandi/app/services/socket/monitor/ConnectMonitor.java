package com.tosslab.jandi.app.services.socket.monitor;

/**
 * Created by Steve SeongUg Jung on 15. 4. 9..
 */
public class ConnectMonitor {

    private OnTimer onTimer;
    private TimerRunnable timerRunnable;

    public ConnectMonitor(OnTimer onTimer) {
        this.onTimer = onTimer;
    }

    synchronized public void start() {
        if (timerRunnable != null) {
            timerRunnable.setRun(false);
            timerRunnable = null;
        }

        timerRunnable = new TimerRunnable(onTimer);
        timerRunnable.setRun(true);
        new Thread(timerRunnable).start();
    }

    synchronized public void stop() {
        if (timerRunnable != null) {
            timerRunnable.setRun(false);
            timerRunnable = null;
        }
    }

    public interface OnTimer {
        void onTime();
    }

    private static class TimerRunnable implements Runnable {

        private final OnTimer onTimer;
        private boolean run;

        private TimerRunnable(OnTimer onTimer) {
            this.onTimer = onTimer;
            run = true;
        }

        public boolean isRun() {
            return run;
        }

        public void setRun(boolean run) {
            this.run = run;
        }

        @Override
        public void run() {
            while (run) {
                try {
                    Thread.sleep(1000 * 10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (run && onTimer != null) {
                    onTimer.onTime();
                }
            }
        }
    }
}
