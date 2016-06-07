package com.tosslab.jandi.app.services.socket.monitor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.services.socket.JandiSocketService;

/**
 * Created by tonyjs on 16. 4. 15..
 */
public class SocketServiceCloser {

    private static final int MESSAGE_ID = 0;
    private static final int DELAY_MILLIS = 1000 * 15; // 15 sec

    private static SocketServiceCloser sInstance;

    private SocketServiceCloseHandler socketServiceCloseHandler;

    private SocketServiceCloser() {
    }

    public static SocketServiceCloser getInstance() {
        if (sInstance == null) {
            sInstance = new SocketServiceCloser();
        }
        return sInstance;
    }

    public void close() {
        if (socketServiceCloseHandler == null) {
            socketServiceCloseHandler = new SocketServiceCloseHandler(Looper.getMainLooper());
        }
        socketServiceCloseHandler.sendEmptyMessageDelayed(MESSAGE_ID, DELAY_MILLIS);
    }

    public void cancel() {
        if (socketServiceCloseHandler == null) {
            return;
        }

        socketServiceCloseHandler.removeMessages(MESSAGE_ID);
        socketServiceCloseHandler = null;
    }

    private static class SocketServiceCloseHandler extends Handler {

        public SocketServiceCloseHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            JandiSocketService.stopService(JandiApplication.getContext());
        }
    }


}
