package com.tosslab.jandi.app.ui.intro.model;

import android.util.Log;

import com.tosslab.jandi.app.ui.intro.IntroActivity;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.httpclient.FakeHttp;

import java.util.concurrent.Callable;

import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func2;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(JandiRobolectricGradleTestRunner.class)
public class IntroActivityModelTest {

    private boolean isCalled;
    private IntroActivityModel introActivityModel;

    @Before
    public void setUp() throws Exception {


        IntroActivity introActivity_ = Robolectric.buildActivity(IntroActivity_.class).get();

        introActivityModel = IntroActivityModel_.getInstance_(introActivity_);

        FakeHttp.getFakeHttpLayer().interceptHttpRequests(false);

        System.setProperty("robolectric.logging", "stdout");
        ShadowLog.stream = System.out;

    }
    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();

    }

    @Test
    public void testRetrieveThisAppVersion() throws Exception {

        // When : Get app version
        int versionCode = introActivityModel.getInstalledAppVersion(RuntimeEnvironment.application);

        // Then : Maybe....show update dialog..Because AndroidManifest.xml is not defined.
        assertThat(versionCode > 0, is(true));

    }

    @Test
    public void testCombineLastest() throws Exception {

        final boolean[] isFinish = {false};

        Observable.combineLatest(Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Log.d("INFO", "hello~1111");
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        subscriber.onNext(true);
                        subscriber.onCompleted();
                    }
                }).start();

            }
        }), Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                Log.d("INFO", "hello~2222");
                subscriber.onNext(false);
                subscriber.onCompleted();
            }
        }), new Func2<Boolean, Boolean, Boolean>() {
            @Override
            public Boolean call(Boolean o, Boolean o2) {

                Log.d("INFO", "combine : " + o + " , " + o2);


                return o & o2;
            }
        }).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                Log.d("INFO", "Subscribe : " + aBoolean);

                isFinish[0] = true;

            }
        });

        await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isFinish[0];
            }
        });

    }

    @Test
    public void testGetLatestVersionInBackground() throws Exception {

        // When : Get App Version from server
        final boolean[] isCalled = new boolean[1];
        final int[] latestVersionInBackground = new int[1];
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    latestVersionInBackground[0] = introActivityModel.getLatestVersionInBackground();
                } catch (RetrofitError e) {
                    e.printStackTrace();
                }
                isCalled[0] = true;
            }
        }).start();

        await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isCalled[0];
            }
        });

        // Then : {App version from server} > 0
        assertThat(latestVersionInBackground[0] > 0, is(true));
    }

}