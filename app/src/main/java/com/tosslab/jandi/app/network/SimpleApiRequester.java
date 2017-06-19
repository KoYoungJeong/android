package com.tosslab.jandi.app.network;

import android.util.Log;

import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.concurrent.Executor;

import rx.Observable;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 15. 10. 6..
 */
public class SimpleApiRequester {

    private static Observable<Executor> observable;

    static {
        observable = Observable.create(
                (Observable.OnSubscribe<Executor>) subscriber ->
                        subscriber.onNext(
                                new Executor() {
                                    @Override
                                    public void execute(Runnable command) {
                                        command.run();
                                        subscriber.onCompleted();
                                    }
                                })).observeOn(Schedulers.io());

    }

    public static void request(final Runnable runnable) {
        request(runnable, null);
    }

    public static void request(final Runnable runnable, Action0 complete) {

        observable.subscribe(executor -> {
            executor.execute(runnable);
        }, throwable -> {
            LogUtil.e(Log.getStackTraceString(throwable));
        }, complete);
    }

}