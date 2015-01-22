package com.tosslab.jandi.app.ui.message.v2.model;

import com.tosslab.jandi.app.events.messages.RefreshNewMessageEvent;

import org.androidannotations.annotations.EBean;

import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 22..
 */
@EBean
public class MessageListTimer {

    private static final int TIMER_PERIOD = 3000;
    private Timer timer;

    public void start() {
        stop();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                EventBus.getDefault().post(new RefreshNewMessageEvent());
            }
        }, TIMER_PERIOD, TIMER_PERIOD);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

}
