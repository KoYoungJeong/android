package com.tosslab.jandi.lib.sprinkler.flush;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.tosslab.jandi.lib.sprinkler.Sprinkler;

/**
 * Created by tonyjs on 15. 7. 20..
 */
public class FlushRetriever {
    public static final String TAG = FlushRetriever.class.getSimpleName();
    //    private static final long INTERVAL = 1000 * 60;
    private static final long INTERVAL = 1000 * 15;
    private boolean isStop = false;
    private Handler handler;
    private Context context;

    public FlushRetriever(Context context) {
        this.context = context.getApplicationContext();
    }

    public void start() {
        HandlerThread handlerThread = new HandlerThread("flush_retrieve_thread");
        handlerThread.start();
        handler = new FlushHandler(handlerThread.getLooper());
        handler.sendEmptyMessageDelayed(0, INTERVAL);
        isStop = false;
    }

    public void stop() {
        quit();
        isStop = true;
    }

    public boolean isStop() {
        return isStop;
    }

    private void quit() {
        handler.getLooper().quit();
        handler = null;
    }

    private class FlushHandler extends Handler {

        public FlushHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Sprinkler.with(context).flush();
            sendEmptyMessageDelayed(0, INTERVAL);
        }
    }
}
