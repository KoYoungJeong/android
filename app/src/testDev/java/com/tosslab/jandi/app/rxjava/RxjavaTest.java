package com.tosslab.jandi.app.rxjava;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;

import java.util.concurrent.TimeUnit;

import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static com.jayway.awaitility.Awaitility.await;

@RunWith(RobolectricGradleTestRunner.class)
public class RxjavaTest {
    @Test
    public void testPublish_collect_throttleTimeout() throws Exception {

        PublishSubject<String> publishSubject = PublishSubject.create();
        publishSubject
                .observeOn(Schedulers.io())
                .buffer(100, TimeUnit.MILLISECONDS)
                .filter(strings -> !strings.isEmpty())
                .subscribe(System.out::println);

        Thread.sleep(100);

        await().until(() -> {
            try {

                for (int idx = 0; idx < 100; ++idx) {
                    publishSubject.onNext(String.valueOf(idx));
                    Thread.sleep(10);
                }

                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

    }
}
