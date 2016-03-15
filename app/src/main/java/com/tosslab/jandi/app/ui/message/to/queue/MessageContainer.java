package com.tosslab.jandi.app.ui.message.to.queue;

/**
 * Created by Steve SeongUg Jung on 15. 3. 5..
 */
public interface MessageContainer<T> {

    LoadType getQueueType();

    T getData();

}
