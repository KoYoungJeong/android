package com.tosslab.jandi.lib.sprinkler.util;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.tosslab.jandi.lib.sprinkler.Sprinkler;

/**
 * Created by tonyjs on 15. 7. 20..
 */
public class FlushRetriever {
    public static final String TAG = Logger.makeTag(FlushRetriever.class);

    private static final long INTERVAL = 1000 * 60;
    //    private static final long INTERVAL = 1000 * 15;
    private boolean isStopped = true;
    private Handler handler;
    private Context context;

    public FlushRetriever(Context context) {
        this.context = context.getApplicationContext();
    }

    public void start() {
        if (!isStopped && handler != null) {
            return;
        }
        HandlerThread handlerThread = new HandlerThread("FlushRetriever.HandlerThread");
        handlerThread.start();
        handler = new FlushHandler(handlerThread.getLooper());
        handler.sendEmptyMessageDelayed(0, INTERVAL);
        isStopped = false;
    }

    public void stop() {
        quit();
        isStopped = true;
    }

    public boolean isStopped() {
        return isStopped;
    }

    private void quit() {
        if (handler == null) {
            return;
        }
        handler.removeMessages(0);
        handler.getLooper().quit();
        handler = null;
    }

    private void flush() {
        Sprinkler.with(context).flush();
    }

    private void scheduleNextFlush() {
        if (handler == null) {
            return;
        }
        handler.sendEmptyMessageDelayed(0, INTERVAL);
    }

    private class FlushHandler extends Handler {

        public FlushHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage");
            flush();
            scheduleNextFlush();
        }
    }
}
