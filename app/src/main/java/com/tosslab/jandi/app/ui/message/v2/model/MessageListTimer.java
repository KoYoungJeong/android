package com.tosslab.jandi.app.ui.message.v2.model;

import com.tosslab.jandi.app.events.messages.RefreshNewMessageEvent;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 22..
 */
@EBean
public class MessageListTimer {

    private static final int TIMER_PERIOD = 3000;
    private Timer timer;
    private Lock lock;

    @AfterInject
    void initObject() {
        lock = new ReentrantLock();

    }

    public void start() {
        stop();

        if (lock.tryLock()) {

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    EventBus.getDefault().post(new RefreshNewMessageEvent());
                }
            }, TIMER_PERIOD, TIMER_PERIOD);

            lock.unlock();
        }
    }

    public void stop() {
        if (lock.tryLock()) {

            if (timer != null) {
                timer.cancel();
                timer.purge();
                timer = null;
            }
            lock.unlock();
        }
    }

}
